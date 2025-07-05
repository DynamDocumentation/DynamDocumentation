package com.dynam.database.tables

import org.jetbrains.exposed.sql.*
import com.dynam.enums.VariableType
import com.dynam.database.tables.Entities
import com.dynam.database.tables.Classes
import com.dynam.database.tables.Functions

object Variables: Table() {
    val id = integer("id").autoIncrement()
    val classId = integer("class_id").references(Classes.id).nullable()
    val functionId = integer("function_id").references(Functions.id).nullable()
    
    // Parameter details
    val type = enumerationByName("type", 255, VariableType::class)
    val name = text("name")
    val dataType = text("data_type").nullable()
    val description = text("description").nullable() 
    val defaultValue = text("default_value").nullable()
    
    override val primaryKey = PrimaryKey(id)
}