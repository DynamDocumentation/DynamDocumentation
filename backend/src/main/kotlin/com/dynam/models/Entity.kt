package com.dynam.models

import com.dynam.database.dbQuery
import com.dynam.database.tables.Entities
import com.dynam.enums.EntityType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select

data class Entity(
    val id: Int, 
    val type: EntityType, 
    val name: String,
    val description: String,
    val namespaceId: Int
) {
    companion object {
        fun fromRow(row: ResultRow) = Entity(
            id = row[Entities.id],
            type = row[Entities.type],
            name = row[Entities.name],
            description = row[Entities.description],
            namespaceId = row[Entities.namespaceId]
        )
        
        suspend fun getAll(): List<Entity> = dbQuery {
            Entities.selectAll().map { fromRow(it) }
        }
        
        suspend fun getAllClasses(): List<Entity> = dbQuery {
            Entities.select { Entities.type eq EntityType.CLASS }.map { fromRow(it) }
        }

        suspend fun getAllFunctions(): List<Entity> = dbQuery {
            Entities.select { Entities.type eq EntityType.FUNCTION }.map { fromRow(it) }
        }
    }
}