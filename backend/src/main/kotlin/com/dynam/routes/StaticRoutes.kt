package com.dynam.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

class StaticRoutes {
    fun registerRoutes(route: Route) {
        route.singlePageApplication {
            react("../frontend/build")
        }
    }
}
