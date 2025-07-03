package com.dynam.repositories

import com.dynam.database.dbQuery
import com.dynam.database.tables.Variables
import com.dynam.enums.VariableType
import com.dynam.dtos.Variable
import org.jetbrains.exposed.sql.*

/**
 * Repository for Variable-related database operations.
 * This class handles all database access for Variable objects.
 */
class VariableRepository {
    /**
     * Convert a database row to a Variable object
     */
    private fun fromRow(row: ResultRow) = Variable(
        id = row[Variables.id],
        entityId = row[Variables.entityId],
        type = row[Variables.type],
        name = row[Variables.name],
        description = row[Variables.description]
    )
    
    /**
     * Get all variables from the database
     */
    suspend fun getAll(): List<Variable> = dbQuery {
        Variables.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get a variable by its ID
     */
    suspend fun getById(id: Int): Variable? = dbQuery {
        Variables.selectAll()
            .where { Variables.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Get variables by entity ID
     */
    suspend fun getByEntityId(entityId: Int): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { Variables.entityId eq entityId }
            .map { fromRow(it) }
    }
    
    /**
     * Get variables by entity ID and type
     */
    suspend fun getByEntityIdAndType(entityId: Int, type: VariableType): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { 
                (Variables.entityId eq entityId) and (Variables.type eq type)
            }
            .map { fromRow(it) }
    }
    
    /**
     * Create a new variable
     */
    suspend fun create(
        entityId: Int,
        type: VariableType,
        name: String,
        description: String
    ): Variable = dbQuery {
        val id = Variables.insert {
            it[Variables.entityId] = entityId
            it[Variables.type] = type
            it[Variables.name] = name
            it[Variables.description] = description
        } get Variables.id
        
        Variable(id, entityId, type, name, description)
    }
}
