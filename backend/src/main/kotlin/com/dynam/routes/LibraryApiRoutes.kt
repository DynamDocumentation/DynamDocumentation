package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import com.dynam.dtos.*
import com.dynam.enums.*
import com.dynam.dtos.table.Class
import com.dynam.dtos.table.Function
import com.dynam.repositories.NamespaceRepository
import com.dynam.repositories.ClassRepository
import com.dynam.repositories.FunctionRepository
import com.dynam.repositories.LibraryRequestRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Serializable
data class NamespaceContent(
    val name: String,
    val classes: List<Class>,
    val functions: List<Function>
)

class LibraryApiRoutes {
    // Create repository instances
    private val namespaceRepository = NamespaceRepository()
    private val classRepository = ClassRepository()
    private val functionRepository = FunctionRepository()
    private val libraryRequestRepository = LibraryRequestRepository()
    private val logger = LoggerFactory.getLogger(LibraryApiRoutes::class.java)
    
    /**
     * Ensures all libraries in the Namespaces table have corresponding entries in the LibraryRequests table
     * marked as accepted. This function is called when the server starts.
     */
    suspend fun ensureLibraryRequestsExist() {
        try {
            logger.info("Checking libraries for LibraryRequests table...")
            
            // Get all unique library names from the Namespaces table
            val libraryNames = namespaceRepository.getAllLibraryNames()
            logger.info("Found ${libraryNames.size} libraries: ${libraryNames.joinToString(", ")}")
            
            var createdCount = 0
            // For each library, check if a LibraryRequest exists and create one if not
            for (libraryName in libraryNames) {
                // Check if library request already exists
                val existingRequest = libraryRequestRepository.getByName(libraryName)
                
                if (existingRequest == null) {
                    // Create a new library request and mark it as accepted
                    logger.info("Creating LibraryRequest for: $libraryName")
                    val newRequest = libraryRequestRepository.create(libraryName)
                    libraryRequestRepository.updateAcceptanceStatus(newRequest.id, true)
                    createdCount++
                } else if (!existingRequest.accepted) {
                    // If request exists but not accepted, mark it as accepted
                    logger.info("Updating existing LibraryRequest for $libraryName to accepted")
                    libraryRequestRepository.updateAcceptanceStatus(existingRequest.id, true)
                    createdCount++
                }
            }
            
            if (createdCount > 0) {
                logger.info("Created/updated $createdCount LibraryRequests")
            } else {
                logger.info("All libraries already have corresponding LibraryRequests")
            }
        } catch (e: Exception) {
            logger.error("Error ensuring LibraryRequests exist: ${e.message}", e)
        }
    }
    
    fun registerRoutes(route: Route) {
        // Ensure all libraries have LibraryRequests entries when the server starts
        runBlocking {
            ensureLibraryRequestsExist()
        }
        
        route.route("/library") {
            // Get all library names
            get {
                try {
                    val libraryNames = namespaceRepository.getAllLibraryNames()
                    call.respond(mapOf("libraries" to libraryNames))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to e.message)
                    )
                }
            }
            
            // Get library content by name
            get("/{libname}") {
                try {
                    val libName = call.parameters["libname"] ?: throw IllegalArgumentException("Library name must be provided")
                    
                    val namespaces = namespaceRepository.getByLibrary(libName)
                    
                    val response = namespaces.map { namespace ->
                        NamespaceContent(
                            name = namespace.name,
                            classes = classRepository.getByNamespace(namespace.id),
                            functions = functionRepository.getDirectNamespaceFunctions(namespace.id)
                        )
                    }
                                    
                    call.respond(response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to e.message)
                    )
                }
            }
        }
    }
}
