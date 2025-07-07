package com.dynam.routes

import io.ktor.server.routing.*
import com.dynam.controllers.EntityController
import io.ktor.server.application.*

class EntityRoutes {
    private val controller = EntityController()

    fun registerRoutes(route: Route) {
        route.route("/class") {
            get("/{classId}") {
                controller.getClassById(call)
            }
        }
        route.route("/function") {
            get("/{functionId}") {
                controller.getFunctionById(call)
            }
        }
    }
}
