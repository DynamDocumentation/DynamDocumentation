package com.dynam.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import com.dynam.database.tables.Namespaces
import com.dynam.database.tables.Variables
import com.dynam.database.tables.Entities
import com.dynam.database.tables.Constants
import com.dynam.database.tables.Users
import com.dynam.database.tables.LibraryRequests

/**
 * Configures the database connection and schema for the application.
 * Creates all necessary tables if they don't exist.
 */
fun Application.configureDatabases() {
    val driverClass = environment.config.property("ktor.storage.driverClassName").getString()
    val jdbcUrl = environment.config.property("ktor.storage.jdbcURL").getString()
    val db = Database.connect(provideDataSource(jdbcUrl, driverClass))
    transaction(db) {
        SchemaUtils.create(Namespaces, Variables, Entities, Constants, Users, LibraryRequests)
    }
}

/**
 * Creates and configures a HikariCP connection pool for database connections.
 */
private fun provideDataSource(url: String, driverClass: String): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        driverClassName = driverClass
        jdbcUrl = url
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    return HikariDataSource(hikariConfig)
}

/**
 * A utility function for executing database operations in a coroutine-friendly way.
 * Uses Exposed's suspended transaction for proper handling of asynchronous database operations.
 */
suspend fun <T> dbQuery(block: suspend ()->T): T {
    return newSuspendedTransaction(Dispatchers.IO) { block() }
}
