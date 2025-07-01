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

object Variables : Table("variables") {
    val id = integer("id").autoIncrement()
    val entityId = integer("entity_id") references Entities.id
    val type = enumerationByName("type", 255, VariableType::class)
    val name = varchar("name", 255)
    val dataType = varchar("data_type", 255).nullable() // Adicione esta linha
    val description = text("description").nullable() 
    val defaultValue = varchar("default_value", 255).nullable()
    
    override val primaryKey = PrimaryKey(id)
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