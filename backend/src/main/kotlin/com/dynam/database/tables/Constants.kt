package com.dynam.database.tables

import org.jetbrains.exposed.sql.*
import com.dynam.enums.VariableType
import com.dynam.database.tables.Entities

object Constants: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val value = varchar("default_value", 255)
    val entityId = reference("entity_id", Entities.id)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}