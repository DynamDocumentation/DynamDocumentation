// src/main/kotlin/com/dynam/routes/UserRoutes.kt
package com.dynam.routes

<<<<<<< Updated upstream
import com.dynam.database.DatabaseSimulator
import com.dynam.models.User
=======
import com.dynam.models.*
>>>>>>> Stashed changes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode 

class UserRoutes() {
    fun registerRoutes(route: Route) {
        route.get("/users") {
            try {
<<<<<<< Updated upstream
                call.respond(db.fetchUsers())
=======
                val namespaces = namespaceModel.getAllNamespaces()
                var response = namespaces.map { namespace -> NamespaceResponse(namespace, classModel.getAllEntityNamesFrom(namespace) + functionModel.getAllEntityNamesFrom(namespace)) }
                call.respond(response)
>>>>>>> Stashed changes
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Falha ao buscar usuários")
                )
            }
        }
    }
}
