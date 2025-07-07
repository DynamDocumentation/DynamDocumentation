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
}
