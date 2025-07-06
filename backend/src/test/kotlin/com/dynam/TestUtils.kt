package com.dynam

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Utility functions for tests.
 */
object TestUtils {
    /**
     * Execute a database query in a test context.
     * This is a replacement for the production dbQuery function to ensure
     * tests use the test database.
     * 
     * @param db The test database instance
     * @param block The query to execute
     * @return The result of the query
     */
    suspend fun <T> dbTestQuery(db: Database, block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO, db = db) { 
            block() 
        }
    }
}
