package com.dynam

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.http.content.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import com.dynam.routes.LibraryRoutes
import com.dynam.database.*
import com.dynam.models.*
import com.dynam.enums.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        anyHost()
    }
}

fun Application.module() {
    // 0) Logs
    install(CallLogging)

    // 1) CORS
    configureCORS()

    // 2) Content negotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            explicitNulls = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    configureDatabases()

    routing {
        singlePageApplication {
            react("../frontend/build")
        }

        // Initialize library routes
        LibraryRoutes().registerRoutes(this)
        
        // API routes
        route("/library") {
            get("/{libname}") {
                try {
                    val libName = call.parameters["libname"] ?: throw IllegalArgumentException("Library name must be provided")
                    val namespaces = Namespace.getByLibrary(libName)
                    
                    val result = mutableMapOf<String, Map<String, List<Entity>>>()
                    
                    for (namespace in namespaces) {
                        val classes = Entity.getEntitiesByNamespaceId(namespace.id, EntityType.CLASS)
                        val functions = Entity.getEntitiesByNamespaceId(namespace.id, EntityType.FUNCTION)
                        
                        result[namespace.name] = mapOf(
                            "classes" to classes,
                            "functions" to functions
                        )
                    }
                    
                    call.respond(result)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to e.message)
                    )
                }
            }
        }

        route("/entity") {
            get("/{entityId}") {
                try {
                    val entityIdParam = call.parameters["entityId"] ?: throw IllegalArgumentException("Entity ID must be provided")
                    val entityId = entityIdParam.toIntOrNull() ?: throw IllegalArgumentException("Entity ID must be a valid integer")
                    
                    // Get the entity by ID
                    val entity = Entity.getById(entityId) ?: throw NoSuchElementException("Entity not found with ID: $entityId")
                    
                    // Get all variables associated with this entity, grouped by type
                    val variables = Entity.getEntityVariables(entityId)
                    
                    // Create a serializable response structure
                    @Serializable
                    data class EntityResponse(
                        val entity: Entity,
                        val attributes: List<Variable>,
                        val parameters: List<Variable>,
                        val returns: List<Variable>
                    )
                    
                    val response = EntityResponse(
                        entity = entity,
                        attributes = variables[VariableType.DESCRIPTION] ?: emptyList(), // Changed from ATTRIBUTE to DESCRIPTION
                        parameters = variables[VariableType.PARAMETER] ?: emptyList(),
                        returns = variables[VariableType.RETURN] ?: emptyList()
                    )
                    
                    call.respond(response)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to e.message)
                    )
                } catch (e: NoSuchElementException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to e.message)
                    )
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