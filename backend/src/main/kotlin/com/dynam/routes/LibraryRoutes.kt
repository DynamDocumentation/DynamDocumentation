package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.serialization.Serializable
import com.dynam.repositories.LibraryRequestRepository
import com.dynam.dtos.ApiResponses

// Data classes for requests
@Serializable
data class LibraryInstallRequest(val libraryName: String)

@Serializable
data class CreateLibraryRequestDto(val name: String)

class LibraryRoutes {
    private val libraryRequestRepository = LibraryRequestRepository()
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
                val request = call.receive<LibraryInstallRequest>()
                val libraryName = request.libraryName
                
                // Install the library using pip directly
                println("Installing library: $libraryName")
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
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("status" to "error", 
                             "message" to "Failed to install library: $output"))
                    return@post
                }
                
                // Execute the Python script directly
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
                    call.respond(mapOf("status" to "success", 
                                      "message" to "Library documented successfully"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, 
                                mapOf("status" to "error", 
                                     "message" to "Failed to document library: $docOutput"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, 
                            mapOf("status" to "error", 
                                 "message" to "Server error: ${e.message}"))
            }
        }
    }
}