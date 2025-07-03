package com.dynam.repositories

import com.dynam.database.dbQuery
import com.dynam.database.tables.Namespaces
import com.dynam.dtos.Namespace
import org.jetbrains.exposed.sql.*

/**
 * Repository for Namespace-related database operations.
 * This class handles all database access for Namespace objects.
 */
class NamespaceRepository {
    /**
     * Convert a database row to a Namespace object
     */
    private fun fromRow(row: ResultRow) = Namespace(
        id = row[Namespaces.id],
        name = row[Namespaces.name]
    )
    
    /**
     * Get all namespaces from the database
     */
    suspend fun getAll(): List<Namespace> = dbQuery {
        Namespaces.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get a namespace by its ID
     */
    suspend fun getById(id: Int): Namespace? = dbQuery {
        Namespaces.selectAll()
            .where { Namespaces.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Get a namespace by its name
     */
    suspend fun getByName(name: String): Namespace? = dbQuery {
        Namespaces.selectAll()
            .where { Namespaces.name eq name }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Get all namespaces for a specific library
     * 
     * @param libraryName The name of the library
     * @return List of namespaces related to the library
     */
    suspend fun getByLibrary(libraryName: String): List<Namespace> = dbQuery {
        // In this implementation, we're assuming the library name is contained in the namespace name
        // You might need to adjust this based on your actual data structure
        Namespaces.selectAll()
            .where { Namespaces.name.lowerCase() like "%${libraryName.lowercase()}%" }
            .map { fromRow(it) }
    }
    
    /**
     * Create a new namespace
     */
    suspend fun create(name: String): Namespace = dbQuery {
        val id = Namespaces.insert {
            it[Namespaces.name] = name
        } get Namespaces.id
        
        Namespace(id, name)
    }
}
