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
    
    fun registerRoutes(route: Route) {
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
