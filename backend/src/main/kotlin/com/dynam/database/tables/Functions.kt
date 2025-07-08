package com.dynam.database.tables

import com.dynam.enums.VariableType
import org.jetbrains.exposed.sql.Table

object Functions : Table() {
    val id = integer("id").autoIncrement()
    val parentClassId = integer("parent_class_id").references(Classes.id).nullable()
    val parentNamespaceId = integer("parent_namespace_id").references(Namespaces.id).nullable()
    val name = text("name")
    val signature = text("signature").nullable()
    val description = text("description").nullable()
    val returnType = text("return_type").nullable()
    val example = text("example").nullable()

    override val primaryKey = PrimaryKey(id)
}