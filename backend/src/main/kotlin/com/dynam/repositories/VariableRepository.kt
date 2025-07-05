package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Variables
import com.dynam.enums.VariableType
import com.dynam.dtos.table.Variable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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
        classId = row[Variables.classId],
        functionId = row[Variables.functionId],
        type = row[Variables.type],
        name = row[Variables.name],
        dataType = row[Variables.dataType],
        description = row[Variables.description],
        defaultValue = row[Variables.defaultValue]
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
     * Get variables by class ID
     */
    suspend fun getByClassId(classId: Int): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { Variables.classId eq classId }
            .map { fromRow(it) }
    }
    
    /**
     * Get variables by function ID
     */
    suspend fun getByFunctionId(functionId: Int): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { Variables.functionId eq functionId }
            .map { fromRow(it) }
    }
    
    /**
     * Get variables by class ID and type
     */
    suspend fun getByClassIdAndType(classId: Int, type: VariableType): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { 
                (Variables.classId eq classId) and (Variables.type eq type)
            }
            .map { fromRow(it) }
    }
    
    /**
     * Get variables by function ID and type
     */
    suspend fun getByFunctionIdAndType(functionId: Int, type: VariableType): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { 
                (Variables.functionId eq functionId) and (Variables.type eq type)
            }
            .map { fromRow(it) }
    }
    
    /**
     * Create a new variable
     */
    suspend fun create(
        classId: Int? = null,
        functionId: Int? = null,
        type: VariableType,
        name: String,
        dataType: String? = null,
        description: String? = null,
        defaultValue: String? = null
    ): Variable = dbQuery {
        val id = Variables.insert {
            it[Variables.classId] = classId
            it[Variables.functionId] = functionId
            it[Variables.type] = type
            it[Variables.name] = name
            it[Variables.dataType] = dataType
            it[Variables.description] = description
            it[Variables.defaultValue] = defaultValue
        } get Variables.id
        
        Variable(id, classId, functionId, type, name, dataType, description, defaultValue)
    }
    
    /**
     * Update an existing variable
     */
    suspend fun update(
        id: Int,
        classId: Int? = null,
        functionId: Int? = null,
        type: VariableType? = null,
        name: String? = null,
        dataType: String? = null,
        description: String? = null,
        defaultValue: String? = null
    ): Boolean = dbQuery {
        val updateStatement = Variables.update({ Variables.id eq id }) { statement ->
            classId?.let { statement[Variables.classId] = it }
            functionId?.let { statement[Variables.functionId] = it }
            type?.let { statement[Variables.type] = it }
            name?.let { statement[Variables.name] = it }
            dataType?.let { statement[Variables.dataType] = it }
            description?.let { statement[Variables.description] = it }
            defaultValue?.let { statement[Variables.defaultValue] = it }
        }
        updateStatement > 0
    }
    
    /**
     * Delete a variable by ID
     */
    suspend fun delete(id: Int): Boolean = dbQuery {
        Variables.deleteWhere { Variables.id eq id } > 0
    }
    
    /**
     * Get variables related to a class or function by entity ID
     * This method first tries to find a class with the given ID,
     * and if not found, tries to find a function with that ID.
     * 
     * @param entityId The ID of the class or function
     * @return List of variables associated with the class or function
     */
    suspend fun getByEntityId(entityId: Int): List<Variable> = dbQuery {
        // First, try to find a class with this ID
        val classRepository = ClassRepository()
        val classResult = classRepository.getById(entityId)
        
        if (classResult != null) {
            // It's a class, get variables by class ID
            return@dbQuery Variables.selectAll()
                .where { Variables.classId eq entityId }
                .map { fromRow(it) }
        }
        
        // If not a class, try to find a function
        val functionRepository = FunctionRepository()
        val functionResult = functionRepository.getById(entityId)
        
        if (functionResult != null) {
            // It's a function, get variables by function ID
            return@dbQuery Variables.selectAll()
                .where { Variables.functionId eq entityId }
                .map { fromRow(it) }
        }
        
        // If we couldn't find a class or function with this ID, return an empty list
        emptyList()
    }
}
