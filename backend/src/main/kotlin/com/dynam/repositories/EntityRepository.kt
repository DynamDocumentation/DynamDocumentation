package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Entities
import com.dynam.database.tables.Variables
import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import com.dynam.dtos.table.Entity
import com.dynam.dtos.table.Variable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Repository for Entity-related database operations.
 * This class handles all database access for Entity objects.
 */
class EntityRepository {
    /**
     * Convert a database row to an Entity object
     */
    private fun fromRow(row: ResultRow) = Entity(
        id = row[Entities.id],
        type = row[Entities.type],
        name = row[Entities.name],
        description = row[Entities.description],
        namespaceId = row[Entities.namespaceId]
    )
    
    /**
     * Get all entities from the database
     */
    suspend fun getAll(): List<Entity> = dbQuery {
        Entities.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get all entities of type CLASS
     */
    suspend fun getAllClasses(): List<Entity> = dbQuery {
        Entities.selectAll()
            .where { Entities.type eq EntityType.CLASS }
            .map { fromRow(it) }
    }

    /**
     * Get all entities of type FUNCTION
     */
    suspend fun getAllFunctions(): List<Entity> = dbQuery {
        Entities.selectAll()
            .where { Entities.type eq EntityType.FUNCTION }
            .map { fromRow(it) }
    }
    
    /**
     * Get an entity by its ID
     */
    suspend fun getById(id: Int): Entity? = dbQuery {
        Entities.selectAll()
            .where { Entities.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }

    /**
     * Get all entities in a namespace
     */
    suspend fun getByNamespace(namespaceId: Int): List<Entity> = dbQuery {
        Entities.selectAll()
            .where { Entities.namespaceId eq namespaceId }
            .map { fromRow(it) }
    }
    
    /**
     * Get entities by namespace ID and type
     * 
     * @param namespaceId The ID of the namespace
     * @param type The type of entities to retrieve
     * @return List of entities matching the namespace and type
     */
    suspend fun getByNamespaceAndType(namespaceId: Int, type: EntityType): List<Entity> = dbQuery {
        Entities.selectAll()
            .where { 
                (Entities.namespaceId eq namespaceId) and (Entities.type eq type)
            }
            .map { fromRow(it) }
    }
    
    /**
     * Get variables associated with a class entity, grouped by type
     */
    suspend fun getClassVariables(classId: Int): Map<VariableType, List<Variable>> = dbQuery {
        // Get all variables for this class
        val variables = Variables.selectAll()
            .where { Variables.classId eq classId }
            .map { 
                Variable(
                    id = it[Variables.id],
                    classId = it[Variables.classId],
                    functionId = it[Variables.functionId],
                    type = it[Variables.type],
                    name = it[Variables.name],
                    dataType = it[Variables.dataType],
                    description = it[Variables.description],
                    defaultValue = it[Variables.defaultValue]
                )
            }
        
        // Group variables by their type
        variables.groupBy { it.type }
    }
    
    /**
     * Get variables associated with a function entity, grouped by type
     */
    suspend fun getFunctionVariables(functionId: Int): Map<VariableType, List<Variable>> = dbQuery {
        // Get all variables for this function
        val variables = Variables.selectAll()
            .where { Variables.functionId eq functionId }
            .map { 
                Variable(
                    id = it[Variables.id],
                    classId = it[Variables.classId],
                    functionId = it[Variables.functionId],
                    type = it[Variables.type],
                    name = it[Variables.name],
                    dataType = it[Variables.dataType],
                    description = it[Variables.description],
                    defaultValue = it[Variables.defaultValue]
                )
            }
        
        // Group variables by their type
        variables.groupBy { it.type }
    }

    /**
     * Create a new entity
     */
    suspend fun create(
        type: EntityType,
        name: String,
        description: String,
        namespaceId: Int
    ): Entity = dbQuery {
        val id = Entities.insert {
            it[Entities.type] = type
            it[Entities.name] = name
            it[Entities.description] = description
            it[Entities.namespaceId] = namespaceId
        } get Entities.id
        
        Entity(id, type, name, description, namespaceId)
    }
    
    /**
     * Get or create an entity representing a namespace
     * 
     * @param namespaceName The name of the namespace
     * @param namespaceId The ID of the namespace
     * @param description The description for the namespace entity
     * @return The existing or newly created entity
     */
    suspend fun getOrCreateNamespaceEntity(namespaceName: String, namespaceId: Int, description: String): Entity {
        // Check if an entity with this name already exists for the namespace
        val existingEntities = getByNamespaceAndType(namespaceId, EntityType.CLASS)
        val existingEntity = existingEntities.find { it.name == namespaceName }
        
        // Return existing entity or create new one
        return existingEntity ?: create(
            type = EntityType.CLASS,
            name = namespaceName,
            description = description,
            namespaceId = namespaceId
        )
    }
}
