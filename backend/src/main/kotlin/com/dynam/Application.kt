package com.dynam

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.* // Adicione esta linha
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.CallLogging // Import correto

import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import com.dynam.database.DatabaseSimulator
import com.dynam.routes.UserRoutes

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // 0) Logs
    install(CallLogging) // Instalação correta

    // 1) CORS
    install(CORS) {
        allowHost("localhost:3000") // Frontend React
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
    }

    // 2) JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            explicitNulls = false
        })
    }

    // 3) Tratamento de erros
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    // 4) Banco de dados simulado
    val db = DatabaseSimulator()

    // 5) Rotas
    routing {
        UserRoutes(db).registerRoutes(this)
    }
}
