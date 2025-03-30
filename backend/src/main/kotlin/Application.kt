package com.dynam

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }
    
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respond(mapOf(
                "message" to "Backend com Ktor funcionando!",
                "status" to "OK",
                "version" to "1.0"
            ))
        }

        get("/users") {
            call.respond(listOf(
                User(1, "Alice"),
                User(2, "Bob"),
                User(3, "Charlie")
            ))
        }
    }
}

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String? = null,
    val active: Boolean = true
)
