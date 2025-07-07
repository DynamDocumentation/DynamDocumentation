package com.dynam.controllers

import com.dynam.dtos.table.Class
import com.dynam.dtos.table.Function
import com.dynam.repositories.NamespaceRepository
import com.dynam.repositories.ClassRepository
import com.dynam.repositories.FunctionRepository
import com.dynam.repositories.LibraryRequestRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Serializable
data class NamespaceContent(
    val name: String,
    val classes: List<Class>,
    val functions: List<Function>
)

class LibraryApiController {
    private val namespaceRepository = NamespaceRepository()
    private val classRepository = ClassRepository()
    private val functionRepository = FunctionRepository()
    private val libraryRequestRepository = LibraryRequestRepository()
    private val logger = LoggerFactory.getLogger(LibraryApiController::class.java)

    suspend fun ensureLibraryRequestsExist() {
        try {
            logger.info("Checking libraries for LibraryRequests table...")
            val libraryNames = namespaceRepository.getAllLibraryNames()
            logger.info("Found ${libraryNames.size} libraries: ${libraryNames.joinToString(", ")}")
            var createdCount = 0
            for (libraryName in libraryNames) {
                val existingRequest = libraryRequestRepository.getByName(libraryName)
                if (existingRequest == null) {
                    logger.info("Creating LibraryRequest for: $libraryName")
                    val newRequest = libraryRequestRepository.create(libraryName)
                    libraryRequestRepository.updateAcceptanceStatus(newRequest.id, true)
                    createdCount++
                } else if (!existingRequest.accepted) {
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

    suspend fun getLibraries(call: ApplicationCall) {
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

    suspend fun getLibraryContent(call: ApplicationCall) {
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
