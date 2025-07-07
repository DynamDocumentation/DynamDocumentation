package com.dynam

import com.dynam.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base class for tests that require database access.
 * This class sets up and tears down an isolated in-memory SQLite database for testing.
 * This ensures tests never touch the production database.
 */
abstract class DatabaseTest {
    private lateinit var dataSource: HikariDataSource
    protected lateinit var db: Database

    @BeforeTest
    fun setupDatabase() {
        val uniqueDbName = "testdb_" + UUID.randomUUID().toString().replace("-", "")
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:file:$uniqueDbName?mode=memory&cache=shared"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
            isAutoCommit = false
        }
        dataSource = HikariDataSource(config)
        db = Database.connect(dataSource)
        transaction(db) {
            SchemaUtils.create(
                Namespaces,
                Classes,
                Functions,
                Variables,
                ProcessedFiles,
                LibraryRequests,
                Users
            )
        }
    }

    /**
     * Helper function to run coroutine tests
     */
    fun <T> runTest(block: suspend () -> T): T {
        return runBlocking { block() }
    }

    @AfterTest
    fun tearDown() {
        dataSource.close()
    }
}
