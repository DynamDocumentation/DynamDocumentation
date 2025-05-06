package com.dynam.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import com.dynam.database.*

import com.dynam.utils.*

class NamespaceModel : NamespaceModelFacade {
    private fun resultToNamespace(row: ResultRow): Namespace {
        return Namespace(
            id = row[Namespaces.id],
            name = row[Namespaces.name]
        )
    }

    override suspend fun getAllNamespaces() : List<Namespace> = dbQuery {
        Namespaces.selectAll().map { resultToNamespace(it) }
    }
}

object Namespaces: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}