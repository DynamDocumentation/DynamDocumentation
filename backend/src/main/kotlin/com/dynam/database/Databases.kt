package com.dynam.database
import kotlinx.coroutines.delay
import com.dynam.utils.ImportUtil
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import com.dynam.database.tables.*
import java.io.File
import org.slf4j.LoggerFactory

fun Application.configureDatabases() {
    val logger = LoggerFactory.getLogger("DatabaseInitializer")
    val driverClassName = environment.config.property("ktor.storage.driverClassName").getString()
    val jdbcURL = environment.config.property("ktor.storage.jdbcURL").getString()
    
    try {
        val db = Database.connect(provideDataSource(jdbcURL, driverClassName))
        
        // Create tables
        transaction(db) {
            // Adicione esta linha para apagar as tabelas antes de recriar
            SchemaUtils.drop(Namespaces, Entities, Variables, Constants, ProcessedFiles)
            SchemaUtils.create(Namespaces, Entities, Variables, Constants, ProcessedFiles)
            logger.info("Database tables recreated successfully")
        }
        
        // Import data from JSON files automatically
        importDataFromOutputFolder(logger)
    } catch (e: Exception) {
        logger.error("Error initializing database: ${e.message}", e)
        // Usando SQLite como fallback
        try {
            val db = configureSQLiteConnection("jdbc:sqlite:kls_database.db")
            transaction(db) {
                SchemaUtils.create(Namespaces, Entities, Variables, Constants, ProcessedFiles)
                logger.info("Database tables created with SQLite fallback")
            }
            importDataFromOutputFolder(logger)
        } catch (e: Exception) {
            logger.error("Failed to initialize SQLite fallback database: ${e.message}", e)
        }
    }
}

private fun importDataFromOutputFolder(logger: org.slf4j.Logger) {
    val importUtil = ImportUtil()
    val outputDir = File("output")
    
    if (outputDir.exists() && outputDir.isDirectory) {
        logger.info("Starting automatic import of files from 'output' folder")
        
        runBlocking {
            // Processa diretórios e arquivos sequencialmente
            outputDir.listFiles()?.filter { it.isDirectory }?.forEach { libraryDir ->
                libraryDir.listFiles()?.filter { it.extension == "json" }?.forEach { jsonFile ->
                    try {
                        importUtil.importSingleFile(jsonFile.absolutePath)
                        // Pequena pausa entre arquivos para evitar contenção
                        delay(100)
                    } catch (e: Exception) {
                        logger.error("Error importing file ${jsonFile.name}: ${e.message}", e)
                    }
                }
            }
        }
        
        logger.info("Automatic import completed")
    } else {
        logger.warn("Directory 'output' not found. No libraries were automatically imported.")
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

// Adicione esta função para melhorar a configuração do SQLite
private fun configureSQLiteConnection(jdbcUrl: String): Database {
    return Database.connect(jdbcUrl, "org.sqlite.JDBC", 
        setupConnection = { conn ->
            // Configurações SQLite usando a API correta
            conn.createStatement().execute("PRAGMA journal_mode = WAL")
            conn.createStatement().execute("PRAGMA synchronous = NORMAL")
            conn.createStatement().execute("PRAGMA busy_timeout = 10000")
            conn
        }
    )
}

suspend fun <T> dbQuery(block: suspend ()->T): T {
    return newSuspendedTransaction(Dispatchers.IO) { block() }
}