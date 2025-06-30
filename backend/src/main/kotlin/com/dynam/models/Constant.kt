package com.dynam.models

import com.dynam.database.dbQuery
import com.dynam.database.tables.*
import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Constant(
    val id: Int,
    val entityId: Int,
    val name: String,
    val value: String
) {
    companion object {
        fun fromRow(row: ResultRow) = Constant(
            id = row[Constants.id],
            entityId = row[Constants.entityId],
            name = row[Constants.name],
            value = row[Constants.value]
        )
        
        // suspend fun create(
        //     entityId: Int,
        //     name: String,
        //     value: String
        // ): Constant = dbQuery {
        //     val id = Constants.insert {
        //         it[Constants.entityId] = entityId
        //         it[Constants.name] = name
        //         it[Constants.value] = value
        //     } get Constants.id
            
        //     Constant(id, entityId, name, value)
        // }
    }
}