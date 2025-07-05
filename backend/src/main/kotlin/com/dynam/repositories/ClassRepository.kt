package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Classes
import com.dynam.dtos.table.Class
import org.jetbrains.exposed.sql.*

/**
 * Repository for Class-related database operations.
 * This class handles all database access for Class objects.
 */
class ClassRepository {
    /**
     * Convert a database row to a Class object
     */
    private fun fromRow(row: ResultRow) = Class(
        id = row[Classes.id],
        namespaceId = row[Classes.namespaceId],
        name = row[Classes.name],
        description = row[Classes.description],
        signature = row[Classes.signature],
        returnType = row[Classes.returnType],
        example = row[Classes.example]
    )
    
    /**
     * Get all classes from the database
     */
    suspend fun getAll(): List<Class> = dbQuery {
        Classes.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get a class by its ID
     */
    suspend fun getById(id: Int): Class? = dbQuery {
        Classes.selectAll()
            .where { Classes.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }

    /**
     * Get all classes in a namespace
     */
    suspend fun getByNamespace(namespaceId: Int): List<Class> = dbQuery {
        Classes.selectAll()
            .where { Classes.namespaceId eq namespaceId }
            .map { fromRow(it) }
    }
    
    /**
     * Get classes by library name (using namespace pattern matching)
     */
    suspend fun getByLibrary(libraryName: String): List<Class> = dbQuery {
        val pattern = "%${libraryName.lowercase()}%"
        
        (Classes innerJoin com.dynam.database.tables.Namespaces)
            .selectAll()
            .where { com.dynam.database.tables.Namespaces.name.lowerCase() like pattern }
            .map { fromRow(it) }
    }
}
