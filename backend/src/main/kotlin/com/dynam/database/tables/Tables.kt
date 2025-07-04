package com.dynam.database.tables

import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

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

object Classes : Table() {
    val id = integer("id").autoIncrement()
    val namespaceId = integer("namespace_id").references(Namespaces.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val signature = text("signature").nullable()
    val returnType = text("return_type").nullable()
    val example = text("example").nullable()
    override val primaryKey = PrimaryKey(id)
}

object Functions : Table() {
    val id = integer("id").autoIncrement()
    val parentClassId = integer("parent_class_id").references(Classes.id).nullable()
    val parentNamespaceId = integer("parent_namespace_id").references(Namespaces.id).nullable()
    val name = varchar("name", 255)
    val signature = text("signature").nullable()
    val description = text("description").nullable()
    val returnType = text("return_type").nullable()
    val example = text("example").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Variables : Table("Variables") {
    val id = integer("id").autoIncrement()
    // Remove the entityId reference to Entities
    // val entityId = integer("entity_id") references Entities.id
    
    // Add optional references to both Classes and Functions
    val classId = integer("class_id").references(Classes.id).nullable()
    val functionId = integer("function_id").references(Functions.id).nullable()
    
    // Parameter details
    val type = enumerationByName("type", 255, VariableType::class)
    val name = varchar("name", 255)
    val dataType = varchar("data_type", 255).nullable()
    val description = text("description").nullable() 
    val defaultValue = varchar("default_value", 255).nullable()
    
    override val primaryKey = PrimaryKey(id)
    
    // Add a check constraint to ensure either classId or functionId is not null
    // (This is a comment because Exposed doesn't support check constraints directly)
    // In SQL: CHECK (class_id IS NOT NULL OR function_id IS NOT NULL)
}

object Constants : Table() {
    val id = integer("id").autoIncrement()
    val entityId = integer("entity_id").references(Entities.id)
    val name = varchar("name", 255)
    val value = text("value")
    
    override val primaryKey = PrimaryKey(id)
}

object ProcessedFiles : Table() {
    val id = integer("id").autoIncrement()
    val filePath = varchar("file_path", 500)
    val hash = varchar("hash", 64)
    val processedAt = datetime("processed_at").clientDefault { java.time.LocalDateTime.now() }
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(filePath)
    }
}