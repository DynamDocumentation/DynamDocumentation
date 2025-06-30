package com.dynam

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.dynam.routes.LibraryRoutes
import com.dynam.routes.LibraryApiRoutes
import com.dynam.routes.EntityRoutes
import com.dynam.routes.StaticRoutes
import com.dynam.database.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        anyHost()
    }
}

fun Application.module() {
    // 0) Logs
    install(CallLogging)

    // 1) CORS
    configureCORS()

    // 2) Content negotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    configureDatabases()

    routing {
        // Static content (React frontend)
        StaticRoutes().registerRoutes(this)
        
        // API Routes
        LibraryRoutes().registerRoutes(this)
        LibraryApiRoutes().registerRoutes(this)
        EntityRoutes().registerRoutes(this)
    }
}