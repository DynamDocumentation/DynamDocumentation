package com.dynam.database

import com.dynam.database.tables.Constants
import com.dynam.database.tables.Entities
import com.dynam.database.tables.Namespaces
import com.dynam.database.tables.Variables
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

/**
 * Utility class for setting up and managing a test database.
 * This uses SQLite in-memory database for fast and isolated testing.
 */
object TestDatabase {
    /**
     * Initialize an in-memory SQLite database for testing
     * @return The initialized Database instance
     */
    fun init(): Database {
        // Create an in-memory SQLite database
        val db = Database.connect(
            url = "jdbc:sqlite:file:test?mode=memory&cache=shared",
            driver = "org.sqlite.JDBC"
        )
        
        // SQLite uses a different transaction isolation level
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        
        // Create tables
        transaction(db) {
            SchemaUtils.create(Namespaces, Variables, Entities, Constants)
        }
        
        return db
    }
    
    /**
     * Clean up the test database by dropping all tables
     * @param db The database to clean up
     */
    fun cleanup(db: Database) {
        transaction(db) {
            SchemaUtils.drop(Constants, Variables, Entities, Namespaces)
        }
    }
    
    /**
     * Execute a database query in a suspended transaction specifically for tests
     */
    suspend fun <T> dbTest(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }
}
