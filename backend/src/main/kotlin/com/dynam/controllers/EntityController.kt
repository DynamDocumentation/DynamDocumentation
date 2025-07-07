package com.dynam.controllers

import com.dynam.dtos.table.Class
import com.dynam.dtos.table.Function
import com.dynam.dtos.table.Variable
import com.dynam.enums.VariableType
import com.dynam.repositories.ClassRepository
import com.dynam.repositories.FunctionRepository
import com.dynam.repositories.VariableRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

class EntityController {
    private val classRepository = ClassRepository()
    private val functionRepository = FunctionRepository()
    private val variableRepository = VariableRepository()

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

    suspend fun getClassById(call: ApplicationCall) {
        try {
            val classIdParam = call.parameters["classId"] ?: throw IllegalArgumentException("Class ID must be provided")
            val classId = classIdParam.toIntOrNull() ?: throw IllegalArgumentException("Class ID must be a valid integer")
            val classEntity = classRepository.getById(classId)
                ?: throw NoSuchElementException("Class not found with ID: $classId")
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

    suspend fun getFunctionById(call: ApplicationCall) {
        try {
            val functionIdParam = call.parameters["functionId"] ?: throw IllegalArgumentException("Function ID must be provided")
            val functionId = functionIdParam.toIntOrNull() ?: throw IllegalArgumentException("Function ID must be a valid integer")
            val functionEntity = functionRepository.getById(functionId)
                ?: throw NoSuchElementException("Function not found with ID: $functionId")
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
