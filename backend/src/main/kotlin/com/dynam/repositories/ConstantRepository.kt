package com.dynam.repositories

import com.dynam.database.dbQuery
import com.dynam.database.tables.Constants
import com.dynam.dtos.Constant
import org.jetbrains.exposed.sql.*

/**
 * Repository for Constant-related database operations.
 * This class handles all database access for Constant objects.
 */
class ConstantRepository {
    /**
     * Convert a database row to a Constant object
     */
    private fun fromRow(row: ResultRow) = Constant(
        id = row[Constants.id],
        entityId = row[Constants.entityId],
        name = row[Constants.name],
        value = row[Constants.value]
    )
    
    /**
     * Get all constants from the database
     */
    suspend fun getAll(): List<Constant> = dbQuery {
        Constants.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get a constant by its ID
     */
    suspend fun getById(id: Int): Constant? = dbQuery {
        Constants.selectAll()
            .where { Constants.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Get constants by entity ID
     */
    suspend fun getByEntityId(entityId: Int): List<Constant> = dbQuery {
        Constants.selectAll()
            .where { Constants.entityId eq entityId }
            .map { fromRow(it) }
    }
    
    /**
     * Create a new constant
     */
    suspend fun create(
        entityId: Int,
        name: String,
        value: String
    ): Constant = dbQuery {
        val id = Constants.insert {
            it[Constants.entityId] = entityId
            it[Constants.name] = name
            it[Constants.value] = value
        } get Constants.id
        
        Constant(id, entityId, name, value)
    }
}
