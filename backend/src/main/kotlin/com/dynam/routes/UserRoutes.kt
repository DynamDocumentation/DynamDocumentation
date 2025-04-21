// src/main/kotlin/com/dynam/routes/UserRoutes.kt
package com.dynam.routes

import com.dynam.database.DatabaseSimulator
import com.dynam.models.User
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode 

class UserRoutes(private val db: DatabaseSimulator) {
    fun registerRoutes(route: Route) {
        route.get("/users") {
            try {
                call.respond(db.fetchUsers())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Falha ao buscar usuários")
                )
            }
        }
    }
}
