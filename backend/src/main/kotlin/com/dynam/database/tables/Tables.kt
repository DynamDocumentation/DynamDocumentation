package com.dynam.database.tables

import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import org.jetbrains.exposed.sql.Table

object Namespaces : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    
    override val primaryKey = PrimaryKey(id)
}

object Entities : Table() {
    val id = integer("id").autoIncrement()
    val type = enumeration("type", EntityType::class)
    val name = varchar("name", 255)
    val description = text("description")
    val namespaceId = integer("namespace_id").references(Namespaces.id)
    
    override val primaryKey = PrimaryKey(id)
}

object Variables : Table() {
    val id = integer("id").autoIncrement()
    val entityId = integer("entity_id").references(Entities.id)
    val type = enumeration("type", VariableType::class)
    val name = varchar("name", 255)
    val dataType = varchar("data_type", 255).nullable()
    val description = text("description")
    val defaultValue = text("default_value").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object Constants : Table() {
    val id = integer("id").autoIncrement()
    val entityId = integer("entity_id").references(Entities.id)
    val name = varchar("name", 255)
    val value = text("value")
    
    override val primaryKey = PrimaryKey(id)
}