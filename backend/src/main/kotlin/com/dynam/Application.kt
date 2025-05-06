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
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
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

    val namespaceModel = NamespaceModel()
    val classModel = ClassModel()
    val functionModel = FunctionModel()

    routing {
        singlePageApplication {
            react("../frontend/build")
        }

        get("/docs") {
            try {
                val namespaces = namespaceModel.getAllNamespaces()
                var response = namespaces.map { namespace -> NamespaceResponse(namespace, classModel.getAllEntityNamesFrom(namespace) + functionModel.getAllEntityNamesFrom(namespace)) }
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e)
                )
            }
        }
        get("/docs/{namespace}") {
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
