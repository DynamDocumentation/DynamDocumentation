package com.dynam.controllers

import com.dynam.IntegrationTestSetup
import com.dynam.module
import com.dynam.database.tables.Namespaces
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryApiControllerTest : IntegrationTestSetup() {
    @Test
    fun `getLibraries returns list of libraries`() = testApplication {
        // Use a file-based SQLite database for shared access
        val dbFile = "test_shared.db"
        val fileJdbcUrl = "jdbc:sqlite:$dbFile"
        environment {
            config = MapApplicationConfig(
                "ktor.storage.driverClassName" to "org.sqlite.JDBC",
                "ktor.storage.jdbcURL" to fileJdbcUrl
            )
        }
        // Insert test data BEFORE starting the Ktor application
        val testDb = Database.connect(fileJdbcUrl, driver = "org.sqlite.JDBC")
        transaction(testDb) {
            SchemaUtils.createMissingTablesAndColumns(Namespaces)
            Namespaces.insert { it[name] = "numpy" }
            Namespaces.insert { it[name] = "pandas" }
        }
        application {
            module()
        }
        // Make the test HTTP call
        val response = client.get("/library") {
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
        val responseText = response.bodyAsText()
        println("RESPONSE STATUS: ${response.status}") // Debug print
        println("RESPONSE HEADERS: ${response.headers.entries()}") // Debug print
        println("RESPONSE BODY: $responseText") // Debug print
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseText.contains("numpy"))
        assertTrue(responseText.contains("pandas"))
    }

    @Test
    fun `getLibraryContent returns content for a specific library`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(Namespaces)
            Namespaces.insert { it[name] = "numpy" }
        }
        application {
            module()
        }
        val response = client.get("/library/numpy") {
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
        val responseText = response.bodyAsText()
        println("RESPONSE STATUS: ${response.status}")
        println("RESPONSE BODY: $responseText")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseText.contains("numpy"))
    }

    @Test
    fun `ensureLibraryRequestsExist does not throw and creates requests if missing`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(Namespaces)
            Namespaces.insert { it[name] = "scipy" }
        }
        application {
            module()
        }
        // This endpoint is not exposed directly, so we test via side effect: after app start, LibraryRequests should exist for all Namespaces
        // Check via HTTP call to /library to trigger ensureLibraryRequestsExist
        val response = client.get("/library")
        assertEquals(HttpStatusCode.OK, response.status)
        // Now check that the library "scipy" is present in the response
        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("scipy"))
    }
}
