package com.dynam.routes

import com.dynam.database.DatabaseSimulator
import com.dynam.models.User
import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.server.routing.*
import kotlin.test.*

class UserRoutesTest {
    @Test
    fun `test get users should return users`() = testApplication {
        // Set up your routing with the fake database
        application {
            routing {
                UserRoutes(DatabaseSimulator()).registerRoutes(this)
            }
        }

        // Send a GET request to /users
        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)

        // Retrieve and verify the response content
        val content = response.bodyAsText()
        assertTrue(content.contains("João Silva"))
        assertTrue(content.contains("Maria Souza"))
        assertTrue(content.contains("Carlos Oliveira"))
    }
}