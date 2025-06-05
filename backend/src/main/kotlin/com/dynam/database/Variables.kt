package com.dynam.database.tables

import org.jetbrains.exposed.sql.*
import com.dynam.enums.VariableType
import com.dynam.database.tables.Namespaces

object Variables: Table() {
    val id = integer("id").autoIncrement()
    val type = enumeration("type", VariableType::class)
    val name = varchar("name", 255)
    val datatype = varchar("datatype", 255)
    val defaultValue = varchar("default_value", 255)
    val description = varchar("description", 255)
    val namespaceId = reference("namespace_id", Namespaces.id)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}