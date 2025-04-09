// src/main/kotlin/com/dynam/Application.kt
package com.dynam

import io.ktor.server.application.*                  // Application, call
import io.ktor.server.response.*                     // respond
import io.ktor.server.routing.*                      // routing, Route
import io.ktor.server.plugins.contentnegotiation.*   // ContentNegotiation
import io.ktor.serialization.kotlinx.json.*          // json()
import io.ktor.server.plugins.statuspages.*          // StatusPages, exception
import io.ktor.http.*                                // HttpStatusCode
import io.ktor.server.plugins.cors.routing.*

import com.dynam.database.DatabaseSimulator
import com.dynam.routes.UserRoutes

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // 1) JSON
    install(CORS) {
        anyHost() // ⚠️ Apenas para desenvolvimento! Não use isso em produção.
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }
    install(ContentNegotiation) {
        json()
    }

    // 2) Tratamento de erros
    install(StatusPages) {
        // assinatura correta em 2.x: (call, cause) -> Unit
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    // 3) Instancia o simulador
    // 4) Registra rotas (register é extensão em Route)
   val db = DatabaseSimulator()
    routing {
        UserRoutes(db).registerRoutes(this)
    }
  }

