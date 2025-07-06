package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Variables
import com.dynam.enums.VariableType
import com.dynam.dtos.table.Variable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class VariableRepository {
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
    
    suspend fun getAll(): List<Variable> = dbQuery {
        Variables.selectAll().map { fromRow(it) }
    }
    
    suspend fun getById(id: Int): Variable? = dbQuery {
        Variables.selectAll()
            .where { Variables.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun getByClassId(classId: Int): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { Variables.classId eq classId }
            .map { fromRow(it) }
    }
    
    suspend fun getByFunctionId(functionId: Int): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { Variables.functionId eq functionId }
            .map { fromRow(it) }
    }
    
    suspend fun getByClassIdAndType(classId: Int, type: VariableType): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { 
                (Variables.classId eq classId) and (Variables.type eq type)
            }
            .map { fromRow(it) }
    }
    
    suspend fun getByFunctionIdAndType(functionId: Int, type: VariableType): List<Variable> = dbQuery {
        Variables.selectAll()
            .where { 
                (Variables.functionId eq functionId) and (Variables.type eq type)
            }
            .map { fromRow(it) }
    }
    
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
    
    suspend fun delete(id: Int): Boolean = dbQuery {
        Variables.deleteWhere { Variables.id eq id } > 0
    }
}
