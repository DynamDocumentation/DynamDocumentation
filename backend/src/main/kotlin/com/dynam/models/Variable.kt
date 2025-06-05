package com.dynam.models

import com.dynam.database.dbQuery
import com.dynam.database.tables.Variables
import com.dynam.enums.VariableType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class Variable(
    val id: Int, 
    val type: VariableType, 
    val name: String,
    val datatype: String,
    val defaultValue: String,
    val description: String,
    val entityId: Int
) {
    companion object {
        fun fromRow(row: ResultRow) = Variable(
            id = row[Variables.id],
            type = row[Variables.type],
            name = row[Variables.name],
            datatype = row[Variables.datatype],
            defaultValue = row[Variables.defaultValue],
            description = row[Variables.description],
            entityId = row[Variables.entityId]
        )
        
        suspend fun getAll(): List<Variable> = dbQuery {
            Variables.selectAll().map { fromRow(it) }
        }
    }
}
