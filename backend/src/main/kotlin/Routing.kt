package com.dynam

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        singlePageApplication {
            react("../frontend/build")
        }
    }
}
