package com.dynam.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.serialization.Serializable

// Data class for request
@Serializable
data class LibraryRequest(val libraryName: String)

class LibraryRoutes {
    fun registerRoutes(route: Route) {
        route.post("/api/library/install") {
            try {
                val request = call.receive<LibraryRequest>()
                val libraryName = request.libraryName
                
                // Install the library using pip directly
                println("Installing library: $libraryName")
                val installProcess = ProcessBuilder("pip", "install", libraryName)
                    .redirectErrorStream(true)
                    .start()
                
                // Capture output for logging
                val installReader = BufferedReader(InputStreamReader(installProcess.inputStream))
                val output = StringBuilder()
                var line: String?
                while (installReader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                
                val installExitCode = installProcess.waitFor()
                if (installExitCode != 0) {
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("status" to "error", 
                             "message" to "Failed to install library: $output"))
                    return@post
                }
                
                // Execute the Python script directly
                val docProcess = ProcessBuilder("python3", "python/pop_general.py", libraryName)
                    .redirectErrorStream(true)
                    .start()
                
                // Capture output
                val docReader = BufferedReader(InputStreamReader(docProcess.inputStream))
                val docOutput = StringBuilder()
                while (docReader.readLine().also { line = it } != null) {
                    docOutput.append(line).append("\n")
                }
                
                val docExitCode = docProcess.waitFor()
                if (docExitCode == 0) {
                    call.respond(mapOf("status" to "success", 
                                      "message" to "Library documented successfully"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, 
                                mapOf("status" to "error", 
                                     "message" to "Failed to document library: $docOutput"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, 
                            mapOf("status" to "error", 
                                 "message" to "Server error: ${e.message}"))
            }
        }
    }
}