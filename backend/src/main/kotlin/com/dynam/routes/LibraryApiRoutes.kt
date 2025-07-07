package com.dynam.routes

import io.ktor.server.routing.*
import com.dynam.controllers.LibraryApiController
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

class LibraryApiRoutes {
    private val controller = LibraryApiController()

    fun registerRoutes(route: Route) {
        runBlocking {
            controller.ensureLibraryRequestsExist()
        }
        route.route("/library") {
            get {
                controller.getLibraries(call)
            }
            get("/{libname}") {
                controller.getLibraryContent(call)
            }
        }
    }
}
