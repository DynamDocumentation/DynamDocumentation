package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlinx.serialization.Serializable
import com.dynam.repositories.LibraryRequestRepository
import com.dynam.repositories.UserRepository
import com.dynam.dtos.ApiResponse
import com.dynam.dtos.ApiResponses
import com.dynam.dtos.AuthToken
import java.util.Base64

// Data classes for requests
@Serializable
data class LibraryInstallRequest(val requestId: Int, val authToken: String)

@Serializable
data class CreateLibraryRequestDto(val name: String)

class LibraryRoutes {
    private val libraryRequestRepository = LibraryRequestRepository()
    private val userRepository = UserRepository()
    
    /**
     * Validates an authentication token.
     * Returns a Pair of (isValid, errorMessage)
     * If isValid is true, the token is valid and errorMessage is null.
     * If isValid is false, the token is invalid and errorMessage contains the reason.
     */
    private suspend fun validateAuthToken(authToken: String): Pair<Boolean, String?> {
        if (authToken.isBlank()) {
            return Pair(false, "Auth token is required")
        }
        
        return try {
            // Decode and validate the token
            val tokenData = String(Base64.getDecoder().decode(authToken)).split(":")
            if (tokenData.size != 3) {
                return Pair(false, "Invalid token format")
            }
            
            val userId = tokenData[0].toInt()
            val timestamp = tokenData[1].toLong()
            val email = tokenData[2]
            
            // Check if token is expired (24 hours validity)
            val currentTime = System.currentTimeMillis()
            val tokenAge = currentTime - timestamp
            val tokenValid = tokenAge < 24 * 60 * 60 * 1000 // 24 hours in milliseconds
            
            if (!tokenValid) {
                return Pair(false, "Auth token expired")
            }
            
            // Verify the user exists
            val user = userRepository.getById(userId)
            if (user == null || user.email != email) {
                return Pair(false, "Invalid auth token")
            }
            
            // Token is valid
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, "Invalid auth token format")
        }
    }
    
    fun registerRoutes(route: Route) {
        // Endpoint to get all library requests
        route.get("/api/library/requests") {
            try {
                // Get all library requests from repository
                val requests = libraryRequestRepository.getAll()
                
                // Return the list of library requests using ApiResponse
                call.respond(ApiResponses.success(requests))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponses.error("Failed to retrieve library requests: ${e.message}")
                )
            }
        }
        
        // Endpoint to create a new library request
        route.post("/api/library/requests") {
            try {
                // Parse the request body
                val requestDto = call.receive<CreateLibraryRequestDto>()
                
                // Validate the input
                if (requestDto.name.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponses.error("Library name cannot be empty")
                    )
                    return@post
                }
                
                // Check if a request for this library already exists
                val existingRequest = libraryRequestRepository.getByName(requestDto.name)
                if (existingRequest != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ApiResponses.error("A request for this library already exists")
                    )
                    return@post
                }
                
                // Create the new library request
                val createdRequest = libraryRequestRepository.create(requestDto.name)
                
                // Return success response with the created request
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponses.success(
                        data = createdRequest,
                        message = "Library request created successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponses.error("Failed to create library request: ${e.message}")
                )
            }
        }
        
        route.post("/api/library/install") {
            try {
                call.application.log.info("Received library install request")
                val request = call.receive<LibraryInstallRequest>()
                val requestId = request.requestId
                
                // Validate the authentication token
                call.application.log.info("Validating auth token")
                val (isValid, errorMessage) = validateAuthToken(request.authToken)
                if (!isValid) {
                    call.application.log.warn("Auth token validation failed: $errorMessage")
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ApiResponses.error(errorMessage ?: "Authentication failed")
                    )
                    return@post
                }
                
                // Get the library request by ID
                call.application.log.info("Getting library request with ID: $requestId")
                val libraryRequest = libraryRequestRepository.getById(requestId)
                if (libraryRequest == null) {
                    call.application.log.warn("Library request not found with ID: $requestId")
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponses.error("Library request not found")
                    )
                    return@post
                }
                
                val libraryName = libraryRequest.name
                call.application.log.info("Installing library: $libraryName")
                
                // Install the library using pip directly
                val installProcess = ProcessBuilder("pip", "install", libraryName)
                    .redirectErrorStream(true)
                    .start()
                
                // Capture output for logging
                val installReader = BufferedReader(InputStreamReader(installProcess.inputStream))
                val output = StringBuilder()
                var line: String?
                while (installReader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                
                val installExitCode = installProcess.waitFor()
                if (installExitCode != 0) {
                    call.application.log.error("Failed to install library: $libraryName")
                    call.respond(
                        HttpStatusCode.InternalServerError, 
                        ApiResponses.error("Failed to install library: $output")
                    )
                    return@post
                }
                
                // Execute the Python script directly
                call.application.log.info("Generating documentation for $libraryName")
                val docProcess = ProcessBuilder("python3", "python/pop_general.py", libraryName)
                    .redirectErrorStream(true)
                    .start()
                
                // Capture output
                val docReader = BufferedReader(InputStreamReader(docProcess.inputStream))
                val docOutput = StringBuilder()
                while (docReader.readLine().also { line = it } != null) {
                    docOutput.append(line).append("\n")
                }
                
                val docExitCode = docProcess.waitFor()
                if (docExitCode == 0) {
                    // Documentation generation successful, now populate the database
                    call.application.log.info("Populating database with $libraryName data")
                    
                    // Step 1: Populate namespaces
                    val workingDir = File(".")
                    val namespaceProcess = ProcessBuilder(
                        "python3", "-c",
                        "import sys; sys.path.append('./python'); from data_create import namespace_pop; namespace_pop.populate_namespaces_from_output('./output', specific_library='$libraryName')"
                    ).directory(workingDir).redirectErrorStream(true).start()
                    
                    val namespaceReader = BufferedReader(InputStreamReader(namespaceProcess.inputStream))
                    val namespaceOutput = StringBuilder()
                    while (namespaceReader.readLine().also { line = it } != null) {
                        namespaceOutput.append(line).append("\n")
                    }
                    
                    if (namespaceProcess.waitFor() != 0) {
                        call.application.log.error("Failed to populate namespaces: $namespaceOutput")
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponses.error("Failed to populate database namespaces: $namespaceOutput")
                        )
                        return@post
                    }
                    
                    // Step 2: Populate entities (classes and functions)
                    val entityProcess = ProcessBuilder(
                        "python3", "-c",
                        "import sys; sys.path.append('./python'); from data_create import entity_pop; entity_pop.populate_entities_from_namespaces('./output', specific_library='$libraryName')"
                    ).directory(workingDir).redirectErrorStream(true).start()
                    
                    val entityReader = BufferedReader(InputStreamReader(entityProcess.inputStream))
                    val entityOutput = StringBuilder()
                    while (entityReader.readLine().also { line = it } != null) {
                        entityOutput.append(line).append("\n")
                    }
                    
                    if (entityProcess.waitFor() != 0) {
                        call.application.log.error("Failed to populate entities: $entityOutput")
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponses.error("Failed to populate database entities: $entityOutput")
                        )
                        return@post
                    }
                    
                    // Step 3: Populate variables
                    val varProcess = ProcessBuilder(
                        "python3", "-c",
                        "import sys; sys.path.append('./python'); from data_create import var_pop; var_pop.populate_variables('./output', specific_library='$libraryName')"
                    ).directory(workingDir).redirectErrorStream(true).start()
                    
                    val varReader = BufferedReader(InputStreamReader(varProcess.inputStream))
                    val varOutput = StringBuilder()
                    while (varReader.readLine().also { line = it } != null) {
                        varOutput.append(line).append("\n")
                    }
                    
                    if (varProcess.waitFor() != 0) {
                        call.application.log.error("Failed to populate variables: $varOutput")
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponses.error("Failed to populate database variables: $varOutput")
                        )
                        return@post
                    }

                    // Update the library request to mark it as accepted/installed
                    libraryRequestRepository.updateAcceptanceStatus(requestId, true)
                    
                    call.application.log.info("Library $libraryName documented and populated in database successfully")
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponses.success(
                            data = libraryRequest,
                            message = "Library documented and added to database successfully"
                        )
                    )
                } else {
                    call.application.log.error("Failed to document library: $libraryName")
                    call.respond(
                        HttpStatusCode.InternalServerError, 
                        ApiResponses.error("Failed to document library: $docOutput")
                    )
                }
            } catch (e: Exception) {
                call.application.log.error("Error during library installation: ${e.message}")
                call.respond(
                    HttpStatusCode.InternalServerError, 
                    ApiResponses.error("Server error: ${e.message}")
                )
            }
        }
    }
}