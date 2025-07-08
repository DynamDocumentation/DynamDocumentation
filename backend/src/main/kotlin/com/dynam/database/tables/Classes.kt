package com.dynam.database.tables

import com.dynam.enums.VariableType
import org.jetbrains.exposed.sql.Table

object Classes : Table() {
    val id = integer("id").autoIncrement()
    val namespaceId = integer("namespace_id").references(Namespaces.id)
    val name = text("name")
    val description = text("description").nullable()
    val signature = text("signature").nullable()
    val returnType = text("return_type").nullable()
    val example = text("example").nullable()

    override val primaryKey = PrimaryKey(id)
}