package com.dynam.database.tables

import org.jetbrains.exposed.sql.*
import com.dynam.enums.VariableType
import com.dynam.database.tables.Entities

object Constants: Table() {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val value = text("default_value")
    val entityId = reference("entity_id", Entities.id)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}