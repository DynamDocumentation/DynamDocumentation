// src/main/kotlin/com/dynam/routes/UserRoutes.kt
package com.dynam.routes

import io.ktor.server.application.*   // traz `call`
import io.ktor.server.response.*     // traz `respond`
import io.ktor.server.routing.*      // traz `Route`, `route`, `get`
import com.dynam.database.DatabaseSimulator

class UserRoutes(private val db: DatabaseSimulator) {
    // método que registra as rotas no Route passado
    fun registerRoutes(route: Route) {
        route.route("/users") {
            get {
                call.respond(db.fetchUsers())
            }
        }
    }
}

