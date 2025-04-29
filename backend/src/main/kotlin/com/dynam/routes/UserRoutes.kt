// src/main/kotlin/com/dynam/routes/UserRoutes.kt
package com.dynam.routes

import com.dynam.database.DatabaseSimulator
import com.dynam.models.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode 

class UserRoutes(private val db: DatabaseSimulator) {
    fun registerRoutes(route: Route) {
        val namespaceModel = NamespaceModel();
        val classModel = ClassModel();
        val functionModel = FunctionModel();
        route.get("/users") {
            try {
                val namespaces = namespaceModel.getNamespaces()
                var response = namespaces.map { namespace -> NamespaceResponse(namespace, classModel.getFromNamespace(namespace) + functionModel.getFromNamespace(namespace)) }
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e)
                )
            }
        }
        route.get("/users/{namespace}") {
            try {
                var response = functionModel.getDetailsOf(call.parameters["namespace"])
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e)
                )
            }
        }
    }
}
