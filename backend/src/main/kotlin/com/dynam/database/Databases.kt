package com.dynam.database;

import com.dynam.models.Namespaces
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val driverClass = environment.config.property("ktor.storage.driverClassName").getString()
    val jdbcUrl = environment.config.property("ktor.storage.jdbcURL").getString()
    val db = Database.connect(provideDataSource(jdbcUrl, driverClass))
    transaction(db) {
        SchemaUtils.create(Namespaces)
    }
}

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

suspend fun <T> dbQuery(block: suspend ()->T): T {
    return newSuspendedTransaction(Dispatchers.IO) { block() }
}

/*

rotate(180°)
translate(2, 2)
desenhaCasa()


 */