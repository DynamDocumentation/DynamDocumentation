package com.dynam.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

import com.dynam.database.tables.*

/**
 * Configures the database connection and schema for the application.
 * Creates all necessary tables if they don't exist.
 */
fun Application.configureDatabases() {
    val logger = LoggerFactory.getLogger("DatabaseInitializer")
    val driverClassName = environment.config.property("ktor.storage.driverClassName").getString()
    val jdbcURL = environment.config.property("ktor.storage.jdbcURL").getString()
    val refreshTables = environment.config.propertyOrNull("ktor.storage.refreshTables")?.getString()?.toBoolean() ?: false

    try {
        val db = Database.connect(provideDataSource(jdbcURL, driverClassName))
        println("Connecting to database with URL: $jdbcURL")
        // Create tables
        transaction(db) {
            if (refreshTables) {
                // Only drop tables if explicitly requested
                // Dropping in correct order to respect foreign key constraints
                SchemaUtils.drop(Users)
                SchemaUtils.drop(ProcessedFiles)
                SchemaUtils.drop(Variables)
                SchemaUtils.drop(Constants)
                SchemaUtils.drop(Functions)
                SchemaUtils.drop(Classes)
                SchemaUtils.drop(Entities)  // Add Entities table in the drop sequence
                SchemaUtils.drop(LibraryRequests)  // Drop LibraryRequests table
                SchemaUtils.drop(Namespaces)
                
                // Create tables in order
                SchemaUtils.create(Namespaces, Entities, Classes, Functions, Variables, Constants, ProcessedFiles, LibraryRequests, Users)
                println("Database tables recreated successfully")
                logger.info("Database tables recreated successfully")
            } else {
                // Just create tables if they don't exist
                SchemaUtils.createMissingTablesAndColumns(Namespaces, Classes, Functions, Variables, Constants, ProcessedFiles, LibraryRequests, Users)
                println("Database tables verified/created successfully")
                logger.info("Database tables verified/created successfully")
            }
        }
        
        // Use Python scripts to populate the database
        if (refreshTables) {
            populateDatabaseWithPythonScripts(logger)
        }
    } catch (e: Exception) {
        logger.error("Error initializing database: ${e.message}", e)
        // Usando SQLite como fallback
        try {
            val db = configureSQLiteConnection("jdbc:sqlite:kls_database.db")
            transaction(db) {
                SchemaUtils.create(Namespaces, Entities, Classes, Functions, Variables, Constants, ProcessedFiles, LibraryRequests, Users)
                logger.info("Database tables created with SQLite fallback")
            }
            // Use Python scripts to populate the fallback SQLite database
            if (refreshTables) {
                populateDatabaseWithPythonScripts(logger)
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize SQLite fallback database: ${e.message}", e)
        }
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

private fun populateDatabaseWithPythonScripts(logger: org.slf4j.Logger) {
    logger.info("Starting database population using Python scripts...")
    
    try {
        // Get the current working directory
        val workingDir = File(System.getProperty("user.dir"), "python")
        if (!workingDir.exists()) {
            logger.warn("Python directory not found at: ${workingDir.absolutePath}")
            return
        }
        
        // Execute each Python module in the correct order, as specified in MainDatabase.py
        
        // 1. Clean tables - though we've already cleaned them in Kotlin, this is optional
        // executePythonScript(workingDir, "data_create/clean_table.py", "clean_table.clean_all()", logger)
        
        // 2. Populate namespaces
        executePythonModule(workingDir, 
            "data_create.namespace_pop", 
            "populate_namespaces_from_output('../output')", 
            logger)
        
        // 3. Populate entities (classes and functions)
        executePythonModule(workingDir, 
            "data_create.entity_pop", 
            "populate_entities_from_namespaces('../output')", 
            logger)
        
        // 4. Populate variables
        executePythonModule(workingDir, 
            "data_create.var_pop", 
            "populate_variables('../output')", 
            logger)
        
        logger.info("Database population completed successfully")
    } catch (e: Exception) {
        logger.error("Error during database population: ${e.message}", e)
        println("Error during database population: ${e.message}")
    }
}

/**
 * Execute a Python module with the specified function call
 */
private fun executePythonModule(workingDir: File, moduleName: String, functionCall: String, logger: org.slf4j.Logger) {
    // Build a Python command that imports the module and calls the function
    val pythonCode = "import $moduleName; $moduleName.$functionCall"
    val command = listOf("python", "-c", pythonCode)
    
    executeProcess(command, workingDir, logger)
}

/**
 * Execute a process with the given command in the specified directory
 */
private fun executeProcess(command: List<String>, workingDir: File, logger: org.slf4j.Logger) {
    val processBuilder = ProcessBuilder(command)
        .directory(workingDir)
        .redirectErrorStream(true)
    
    logger.info("Executing command: ${command.joinToString(" ")}")
    println("Executing: ${command.joinToString(" ")}")
    
    val process = processBuilder.start()
    
    // Read and log the output
    process.inputStream.bufferedReader().use { reader ->
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println("Python: $line")
            logger.info("Python: $line")
        }
    }
    
    // Wait for the process to complete with a reasonable timeout
    if (!process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES)) {
        process.destroyForcibly()
        logger.error("Process timed out after 10 minutes")
        throw RuntimeException("Process timed out after 10 minutes")
    } 
    
    val exitCode = process.exitValue()
    if (exitCode != 0) {
        logger.error("Process failed with exit code: $exitCode")
        throw RuntimeException("Process failed with exit code: $exitCode")
    }
}

private fun configureSQLiteConnection(jdbcURL: String): Database {
    return Database.connect(jdbcURL, "org.sqlite.JDBC", 
        setupConnection = { conn ->
            // Configurações SQLite usando a API correta
            conn.createStatement().execute("PRAGMA journal_mode = WAL")
            conn.createStatement().execute("PRAGMA synchronous = NORMAL")
            conn.createStatement().execute("PRAGMA busy_timeout = 10000")
            conn
        }
    )
}

/**
 * A utility function for executing database operations in a coroutine-friendly way.
 * Uses Exposed's suspended transaction for proper handling of asynchronous database operations.
 */
suspend fun <T> dbQuery(block: suspend ()->T): T {
    return newSuspendedTransaction(Dispatchers.IO) { block() }
}
