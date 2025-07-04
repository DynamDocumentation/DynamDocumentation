package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import com.dynam.dtos.*
import com.dynam.dtos.table.Entity
import com.dynam.dtos.table.Variable
import com.dynam.enums.*
import com.dynam.repositories.EntityRepository
import com.dynam.repositories.VariableRepository

class EntityRoutes {
    // Create instances of the repositories
    private val entityRepository = EntityRepository()
    private val variableRepository = VariableRepository()
    
    fun registerRoutes(route: Route) {
        route.route("/entity") {
            get("/{entityId}") {
                try {
                    val entityIdParam = call.parameters["entityId"] ?: throw IllegalArgumentException("Entity ID must be provided")
                    val entityId = entityIdParam.toIntOrNull() ?: throw IllegalArgumentException("Entity ID must be a valid integer")
                    
                    // Get the entity by ID using the repository
                    val entity = entityRepository.getById(entityId) 
                        ?: throw NoSuchElementException("Entity not found with ID: $entityId")
                    
                    // Get all variables associated with this entity, grouped by type
                    val allVariables = variableRepository.getByEntityId(entityId)
                    val variablesByType = allVariables.groupBy { it.type }
                    
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
                        attributes = variablesByType[VariableType.DESCRIPTION] ?: emptyList(),
                        parameters = variablesByType[VariableType.PARAMETER] ?: emptyList(),
                        returns = variablesByType[VariableType.RETURN] ?: emptyList()
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
