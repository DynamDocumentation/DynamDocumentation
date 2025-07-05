package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import com.dynam.dtos.*
import com.dynam.dtos.table.Class
import com.dynam.dtos.table.Function
import com.dynam.dtos.table.Variable
import com.dynam.enums.VariableType
import com.dynam.repositories.ClassRepository
import com.dynam.repositories.FunctionRepository
import com.dynam.repositories.VariableRepository

class EntityRoutes {
    // Create instances of the repositories
    private val classRepository = ClassRepository()
    private val functionRepository = FunctionRepository()
    private val variableRepository = VariableRepository()
    
    // Create serializable response structures
    @Serializable
    data class ClassResponse(
        val entity: Class,
        val attributes: List<Variable>,
        val parameters: List<Variable>,
        val returns: List<Variable>
    )
    
    @Serializable
    data class FunctionResponse(
        val entity: Function,
        val attributes: List<Variable>,
        val parameters: List<Variable>,
        val returns: List<Variable>
    )
    
    fun registerRoutes(route: Route) {
        // Class routes
        route.route("/class") {
            get("/{classId}") {
                try {
                    val classIdParam = call.parameters["classId"] ?: throw IllegalArgumentException("Class ID must be provided")
                    val classId = classIdParam.toIntOrNull() ?: throw IllegalArgumentException("Class ID must be a valid integer")
                    
                    // Get the class by ID
                    val classEntity = classRepository.getById(classId) 
                        ?: throw NoSuchElementException("Class not found with ID: $classId")
                    
                    // Get variables for this class
                    val variables = variableRepository.getByClassId(classId)
                    val variablesByType = variables.groupBy { it.type }
                    
                    val response = ClassResponse(
                        entity = classEntity,
                        attributes = variablesByType[VariableType.DESCRIPTION] ?: emptyList(),
                        parameters = variablesByType[VariableType.PARAMETER] ?: emptyList(),
                        returns = variablesByType[VariableType.RETURN] ?: emptyList()
                    )
                    
                    call.respond(response)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
        
        // Function routes
        route.route("/function") {
            get("/{functionId}") {
                try {
                    val functionIdParam = call.parameters["functionId"] ?: throw IllegalArgumentException("Function ID must be provided")
                    val functionId = functionIdParam.toIntOrNull() ?: throw IllegalArgumentException("Function ID must be a valid integer")
                    
                    // Get the function by ID
                    val functionEntity = functionRepository.getById(functionId)
                        ?: throw NoSuchElementException("Function not found with ID: $functionId")
                    
                    // Get variables for this function
                    val variables = variableRepository.getByFunctionId(functionId)
                    val variablesByType = variables.groupBy { it.type }
                    
                    val response = FunctionResponse(
                        entity = functionEntity,
                        attributes = variablesByType[VariableType.DESCRIPTION] ?: emptyList(),
                        parameters = variablesByType[VariableType.PARAMETER] ?: emptyList(),
                        returns = variablesByType[VariableType.RETURN] ?: emptyList()
                    )
                    
                    call.respond(response)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
        
        // Keep the old /entity endpoint for backward compatibility
        route.route("/entity") {
            get("/{entityId}") {
                try {
                    val entityIdParam = call.parameters["entityId"] ?: throw IllegalArgumentException("Entity ID must be provided")
                    val entityId = entityIdParam.toIntOrNull() ?: throw IllegalArgumentException("Entity ID must be a valid integer")
                    
                    // Try to find as class first
                    val classEntity = classRepository.getById(entityId)
                    if (classEntity != null) {
                        // Redirect to the class endpoint
                        call.respondRedirect("/class/$entityId")
                        return@get
                    }
                    
                    // If not a class, try as function
                    val functionEntity = functionRepository.getById(entityId)
                    if (functionEntity != null) {
                        // Redirect to the function endpoint
                        call.respondRedirect("/function/$entityId")
                        return@get
                    }
                    
                    // Neither class nor function found
                    throw NoSuchElementException("No class or function found with ID: $entityId")
                    
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
    }
}
