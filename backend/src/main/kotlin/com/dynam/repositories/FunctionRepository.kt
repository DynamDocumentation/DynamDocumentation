package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Functions
import com.dynam.dtos.table.Function
import org.jetbrains.exposed.sql.*

/**
 * Repository for Function-related database operations.
 * This class handles all database access for Function objects.
 */
class FunctionRepository {
    /**
     * Convert a database row to a Function object
     */
    private fun fromRow(row: ResultRow) = Function(
        id = row[Functions.id],
        parentClassId = row[Functions.parentClassId],
        parentNamespaceId = row[Functions.parentNamespaceId],
        name = row[Functions.name],
        signature = row[Functions.signature],
        description = row[Functions.description],
        returnType = row[Functions.returnType],
        example = row[Functions.example]
    )
    
    /**
     * Get all functions from the database
     */
    suspend fun getAll(): List<Function> = dbQuery {
        Functions.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get a function by its ID
     */
    suspend fun getById(id: Int): Function? = dbQuery {
        Functions.selectAll()
            .where { Functions.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }

    /**
     * Get all functions in a namespace (both direct and via classes)
     */
    suspend fun getByNamespace(namespaceId: Int): List<Function> = dbQuery {
        Functions.selectAll()
            .where { 
                (Functions.parentNamespaceId eq namespaceId) or
                (Functions.parentClassId inSubQuery 
                    com.dynam.database.tables.Classes
                        .slice(com.dynam.database.tables.Classes.id)
                        .select { com.dynam.database.tables.Classes.namespaceId eq namespaceId }
                )
            }
            .map { fromRow(it) }
    }
    
    /**
     * Get functions that are direct children of a namespace (not via classes)
     */
    suspend fun getDirectNamespaceFunctions(namespaceId: Int): List<Function> = dbQuery {
        Functions.selectAll()
            .where { Functions.parentNamespaceId eq namespaceId }
            .map { fromRow(it) }
    }
    
    /**
     * Get functions by class ID
     */
    suspend fun getByClass(classId: Int): List<Function> = dbQuery {
        Functions.selectAll()
            .where { Functions.parentClassId eq classId }
            .map { fromRow(it) }
    }
    
    /**
     * Get functions by library name (using namespace pattern matching)
     */
    suspend fun getByLibrary(libraryName: String): List<Function> = dbQuery {
        val pattern = "%${libraryName.lowercase()}%"
        
        (Functions innerJoin com.dynam.database.tables.Namespaces)
            .selectAll()
            .where { com.dynam.database.tables.Namespaces.name.lowerCase() like pattern }
            .map { fromRow(it) }
    }
}
