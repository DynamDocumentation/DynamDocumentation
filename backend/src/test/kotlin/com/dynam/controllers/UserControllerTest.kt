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
import com.dynam.database.tables.Users
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.dynam.dtos.table.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserControllerTest {
    @Test
    fun `registerUser returns bad request for missing email`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
        application {
            module()
        }
        val user = User(null, "", "", "", 0L, 0L)
        val response = client.post("/api/users/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(user))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("required"))
    }

    @Test
    fun `loginUser returns bad request for missing fields`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
        application {
            module()
        }
        val response = client.post("/api/users/login") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("required"))
    }

    @Test
    fun `validateAuth returns bad request for missing token`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
        application {
            module()
        }
        val response = client.post("/api/users/validate-auth") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Auth token is required"))
    }
}
