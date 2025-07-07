package com.dynam.routes

import com.dynam.controllers.UserController
import io.ktor.server.application.*
import io.ktor.server.routing.*

class UserRoutes(
    private val controller: UserController = UserController()
) {
    fun registerRoutes(route: Route) {
        route.route("/api/users") {
            get("/list") {
                controller.listUsers(call)
            }
            post("/register") {
                controller.registerUser(call)
            }
            delete("/remove/{id}") {
                controller.deleteUser(call)
            }
            post("/login") {
                controller.loginUser(call)
            }
            post("/validate-auth") {
                controller.validateAuth(call)
            }
        }
    }
}
