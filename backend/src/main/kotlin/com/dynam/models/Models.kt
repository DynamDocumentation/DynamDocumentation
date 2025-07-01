package com.dynam.models

import com.dynam.database.dbQuery
import com.dynam.database.tables.*
import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

/* -------------------- NAMESPACE -------------------- */

@Serializable
data class Namespace(
    val id: Int,
    val name: String
) {
    companion object {
        fun fromRow(row: ResultRow) = Namespace(
            id = row[Namespaces.id],
            name = row[Namespaces.name]
        )

        suspend fun getAll(): List<Namespace> = dbQuery {
            Namespaces.selectAll().map { fromRow(it) }
        }

        suspend fun getByLibrary(prefix: String): List<Namespace> = dbQuery {
            Namespaces.select { Namespaces.name.like("$prefix%") }.map { fromRow(it) }
        }

        suspend fun create(name: String): Namespace = dbQuery {
            val existingNamespace = Namespaces
                .select { Namespaces.name eq name }
                .map { fromRow(it) }
                .singleOrNull()

            if (existingNamespace != null) {
                return@dbQuery existingNamespace
            }

            val id = Namespaces.insert {
                it[Namespaces.name] = name
            } get Namespaces.id

            Namespace(id, name)
        }
    }
}

/* -------------------- ENTITY -------------------- */

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

        suspend fun getEntitiesByNamespaceId(namespaceId: Int, type: EntityType): List<Entity> = dbQuery {
            Entities.select {
                (Entities.type eq type) and (Entities.namespaceId eq namespaceId)
            }.map { fromRow(it) }
        }

        suspend fun getById(id: Int): Entity? = dbQuery {
            Entities.select { Entities.id eq id }
                .map { fromRow(it) }
                .singleOrNull()
        }

        suspend fun getEntityVariables(entityId: Int): Map<VariableType, List<Variable>> = dbQuery {
            Variables.select { Variables.entityId eq entityId }
                .map { Variable.fromRow(it) }
                .groupBy { it.type }
        }
    }
}

/* -------------------- VARIABLE -------------------- */

@Serializable
data class Variable(
    val id: Int,
    val entityId: Int,
    val type: VariableType,
    val name: String,
    val dataType: String?, // Alterado para nullable
    val description: String?, // Alterado para nullable
    val defaultValue: String? // Já era nullable
) {
    companion object {
        fun fromRow(row: ResultRow) = Variable(
            id = row[Variables.id],
            entityId = row[Variables.entityId],
            type = row[Variables.type],
            name = row[Variables.name],
            dataType = row[Variables.dataType],
            description = row[Variables.description],
            defaultValue = row[Variables.defaultValue]
        )

        suspend fun getAll(): List<Variable> = dbQuery {
            Variables.selectAll().map { fromRow(it) }
        }

        suspend fun create(
            entityId: Int,
            type: VariableType,
            name: String,
            dataType: String?, // Alterado para nullable
            description: String?, // Alterado para nullable
            defaultValue: String?
        ): Variable = dbQuery {
            val id = Variables.insert {
                it[Variables.entityId] = entityId
                it[Variables.type] = type
                it[Variables.name] = name
                it[Variables.dataType] = dataType
                it[Variables.description] = description
                it[Variables.defaultValue] = defaultValue
            } get Variables.id

            Variable(id, entityId, type, name, dataType, description, defaultValue)
        }
    }
}

/* -------------------- CONSTANT -------------------- */

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
}