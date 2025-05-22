package com.dynam

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.* // Adicione esta linha
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.CallLogging // Import correto
import io.ktor.server.http.content.*

import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import com.dynam.database.*
import com.dynam.controllers.*
import com.dynam.routes.*
import com.dynam.models.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // 0) Logs
    install(CallLogging) // Instalação correta

    // 1) CORS
    install(CORS) {
        allowHost("localhost:3000") // Frontend React
        allowHost("127.0.0.1:3000")
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
    }

    // 2) JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            explicitNulls = true
        })
    }

    // 3) Tratamento de erros
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    configureDatabases()

    val navigationController = NavigationController();
    val classModel = ClassModel()
    val functionModel = FunctionModel()

    routing {
        singlePageApplication {
            react("../frontend/build")
        }

        route("/docs") {
            get {
                try {
                    val response = navigationController.getAllPathsForNavigation()
                    call.respond(response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to e)
                    )
                }
            }

            get("/{namespace}") {
                try {
                    var response = functionModel.getDetailsOf(call.parameters["namespace"])
                    call.respond(response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to e)
                    )
                }
            }
        }
    }
}
