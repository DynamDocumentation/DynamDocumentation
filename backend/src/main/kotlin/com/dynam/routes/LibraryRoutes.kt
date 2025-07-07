package com.dynam.routes

import io.ktor.server.routing.*
import com.dynam.controllers.LibraryController
import io.ktor.server.application.*

class LibraryRoutes {
    private val controller = LibraryController()

    fun registerRoutes(route: Route) {
        route.get("/api/library/requests") {
            controller.getAllLibraryRequests(call)
        }
        route.post("/api/library/requests") {
            controller.createLibraryRequest(call)
        }
        route.post("/api/library/install") {
            controller.installLibrary(call)
        }
    }
}