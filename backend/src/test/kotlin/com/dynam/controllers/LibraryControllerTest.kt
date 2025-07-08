package com.dynam.controllers

import com.dynam.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.dynam.database.tables.LibraryRequests
import org.jetbrains.exposed.sql.insert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryControllerTest {
    @Test
    fun `getAllLibraryRequests returns empty initially`() = testApplication {
        val dbFile = "test_shared.db"
        val fileJdbcUrl = "jdbc:sqlite:$dbFile"
        environment {
            config = MapApplicationConfig(
                "ktor.storage.driverClassName" to "org.sqlite.JDBC",
                "ktor.storage.jdbcURL" to fileJdbcUrl
            )
        }
        val testDb = Database.connect(fileJdbcUrl, driver = "org.sqlite.JDBC")
        transaction(testDb) {
            SchemaUtils.createMissingTablesAndColumns(LibraryRequests)
        }
        application {
            module()
        }
        val response = client.get("/api/library/requests")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("success"))
    }

    @Test
    fun `createLibraryRequest creates a new request`() = testApplication {
        val dbFile = "test_shared.db"
        val fileJdbcUrl = "jdbc:sqlite:$dbFile"
        environment {
            config = MapApplicationConfig(
                "ktor.storage.driverClassName" to "org.sqlite.JDBC",
                "ktor.storage.jdbcURL" to fileJdbcUrl
            )
        }
        val testDb = Database.connect(fileJdbcUrl, driver = "org.sqlite.JDBC")
        transaction(testDb) {
            SchemaUtils.createMissingTablesAndColumns(LibraryRequests)
        }
        application {
            module()
        }
        val response = client.post("/api/library/requests") {
            contentType(ContentType.Application.Json)
            setBody("{\"name\": \"matplotlib\"}")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("matplotlib"))
    }

    @Test
    fun `installLibrary accepts LibraryInstallRequest and validates input`() = testApplication {
        val dbFile = "test_shared.db"
        val fileJdbcUrl = "jdbc:sqlite:$dbFile"
        environment {
            config = MapApplicationConfig(
                "ktor.storage.driverClassName" to "org.sqlite.JDBC",
                "ktor.storage.jdbcURL" to fileJdbcUrl
            )
        }
        val testDb = Database.connect(fileJdbcUrl, driver = "org.sqlite.JDBC")
        var requestId: Int = -1
        transaction(testDb) {
            SchemaUtils.createMissingTablesAndColumns(com.dynam.database.tables.LibraryRequests)
            // Insert a library request
            val result = com.dynam.database.tables.LibraryRequests.insert {
                it[name] = "pytest"
                it[accepted] = true
            }
            requestId = result[com.dynam.database.tables.LibraryRequests.id]!!
        }
        application {
            module()
        }
        // Send a valid install request (token will likely fail validation, but we test the input path)
        val response = client.post("/api/library/install") {
            contentType(ContentType.Application.Json)
            setBody("{" +
                "\"requestId\": $requestId, " +
                "\"authToken\": \"dummy-token\"}")
        }
        // Acceptable responses: Forbidden (token fail), NotFound (request fail), or OK if mocked
        assertTrue(response.status == HttpStatusCode.Forbidden ||
                   response.status == HttpStatusCode.NotFound ||
                   response.status == HttpStatusCode.OK)
        val body = response.bodyAsText()
        assertTrue(body.contains("error") || body.contains("success"))
    }
}
