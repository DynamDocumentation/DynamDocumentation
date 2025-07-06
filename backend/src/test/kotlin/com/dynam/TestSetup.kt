package com.dynam

import com.dynam.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base class for tests that require database access.
 * This class sets up and tears down an isolated in-memory SQLite database for testing.
 * This ensures tests never touch the production database.
 */
abstract class DatabaseTest {
    protected lateinit var db: Database
    private var dataSource: HikariDataSource? = null

    @BeforeTest
    fun setup() {
        // Use HikariCP to keep the in-memory SQLite DB alive and shared
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:file:testdb?mode=memory&cache=shared"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1 // Only one connection needed for tests
            isAutoCommit = false
        }
        dataSource = HikariDataSource(config)
        db = Database.connect(dataSource!!)

        // Create all tables needed for testing
        transaction(db) {
            // Create all tables in the proper order to respect foreign key constraints
            SchemaUtils.create(
                Namespaces,
                Entities,
                Classes,
                Functions,
                Variables,
                Constants,
                ProcessedFiles,
                LibraryRequests,
                Users
            )
        }
    }

    @AfterTest
    fun tearDown() {
        transaction(db) {
            // Drop tables in reverse order to respect foreign key constraints
            SchemaUtils.drop(
                Users,
                LibraryRequests,
                ProcessedFiles,
                Constants,
                Variables,
                Functions,
                Classes,
                Entities,
                Namespaces
            )
        }
        dataSource?.close()
    }

    /**
     * Helper function to run coroutine tests
     */
    fun <T> runTest(block: suspend () -> T): T {
        return runBlocking { block() }
    }
}
