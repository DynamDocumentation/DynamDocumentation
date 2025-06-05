package com.dynam.models

import com.dynam.database.dbQuery
import com.dynam.database.tables.Entities
import com.dynam.database.tables.Variables
import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.and

@Serializable
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
        
        suspend fun getEntitiesByNamespaceId(namespaceId: Int, type: EntityType): List<Entity> = dbQuery {
            Entities.select { 
                (Entities.type eq type) and (Entities.namespaceId eq namespaceId)
            }.map { fromRow(it) }
        }
        
        /**
         * Retrieves an entity by its ID
         * 
         * @param id The ID of the entity to retrieve
         * @return The entity with the specified ID, or null if not found
         */
        suspend fun getById(id: Int): Entity? = dbQuery {
            Entities.select { Entities.id eq id }
                .map { fromRow(it) }
                .singleOrNull()
        }
        
        /**
         * Retrieves all variables associated with an entity, grouped by their type
         * (ATTRIBUTE, PARAMETER, RETURN)
         * 
         * @param entityId The ID of the entity to get variables for
         * @return A map where the keys are variable types and values are lists of variables
         */
        suspend fun getEntityVariables(entityId: Int): Map<VariableType, List<Variable>> = dbQuery {
            val variables = Variables.select { Variables.entityId eq entityId }
                .map { Variable.fromRow(it) }
            
            // Group the variables by their type
            return@dbQuery variables.groupBy { it.type }
        }
    }
}