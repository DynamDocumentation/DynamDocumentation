package com.dynam.database.tables

import org.jetbrains.exposed.sql.*
import com.dynam.enums.EntityType
import com.dynam.database.tables.Namespaces

object Entities: Table() {
    val id = integer("id").autoIncrement()
    val type = enumeration("type", EntityType::class)
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    val namespaceId = reference("namespace_id", Namespaces.id)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}