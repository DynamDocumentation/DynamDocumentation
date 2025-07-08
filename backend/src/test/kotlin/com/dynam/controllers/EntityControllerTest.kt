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
import com.dynam.database.tables.Classes
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityControllerTest {
    @Test
    fun `getClassById returns 404 for missing class`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(Classes)
        }
        application {
            module()
        }
        val response = client.get("/class/99999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("not found"))
    }

    @Test
    fun `getFunctionById returns 404 for missing function`() = testApplication {
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
            SchemaUtils.createMissingTablesAndColumns(com.dynam.database.tables.Functions)
        }
        application {
            module()
        }
        val response = client.get("/function/99999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("not found"))
    }

    @Test
    fun `getFunctionById returns FunctionResponse for existing function`() = testApplication {
        val dbFile = "test_shared.db"
        val fileJdbcUrl = "jdbc:sqlite:$dbFile"
        environment {
            config = MapApplicationConfig(
                "ktor.storage.driverClassName" to "org.sqlite.JDBC",
                "ktor.storage.jdbcURL" to fileJdbcUrl
            )
        }
        val testDb = Database.connect(fileJdbcUrl, driver = "org.sqlite.JDBC")
        var functionId: Int = -1
        transaction(testDb) {
            SchemaUtils.createMissingTablesAndColumns(
                com.dynam.database.tables.Functions,
                com.dynam.database.tables.Variables
            )
            // Insert a function
            val result = com.dynam.database.tables.Functions.insert {
                it[name] = "myFunc"
                it[signature] = "(x: Int): Int"
                it[description] = "A test function"
                it[returnType] = "Int"
                it[example] = "myFunc(1)"
            }
            functionId = result[com.dynam.database.tables.Functions.id]!!
            // Insert variables for the function
            com.dynam.database.tables.Variables.insert {
                it[this.functionId] = functionId
                it[type] = com.dynam.enums.VariableType.PARAMETER
                it[name] = "x"
                it[dataType] = "Int"
                it[description] = "input value"
                it[defaultValue] = null
            }
            com.dynam.database.tables.Variables.insert {
                it[this.functionId] = functionId
                it[type] = com.dynam.enums.VariableType.RETURN
                it[name] = "result"
                it[dataType] = "Int"
                it[description] = "output value"
                it[defaultValue] = null
            }
        }
        application {
            module()
        }
        val response = client.get("/function/$functionId")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("myFunc"))
        assertTrue(body.contains("x"))
        assertTrue(body.contains("result"))
    }
}
