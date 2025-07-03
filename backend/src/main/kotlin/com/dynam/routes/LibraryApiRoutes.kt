package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import com.dynam.dtos.*
import com.dynam.enums.*
import com.dynam.dtos.*
import com.dynam.repositories.NamespaceRepository
import com.dynam.repositories.EntityRepository

@Serializable
data class NamespaceContent(
    val name: String,
    val classes: List<Entity>,
    val functions: List<Entity>
)

class LibraryApiRoutes {
    // Create repository instances
    private val namespaceRepository = NamespaceRepository()
    private val entityRepository = EntityRepository()
    
    fun registerRoutes(route: Route) {
        route.route("/library") {
            get("/{libname}") {
                try {
                    val libName = call.parameters["libname"] ?: throw IllegalArgumentException("Library name must be provided")
                    
                    val namespaces = namespaceRepository.getByLibrary(libName)
                    
                    val response = namespaces.map { namespace ->
                        NamespaceContent(
                            name = namespace.name,
                            classes = entityRepository.getByNamespaceAndType(namespace.id, EntityType.CLASS),
                            functions = entityRepository.getByNamespaceAndType(namespace.id, EntityType.FUNCTION)
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
