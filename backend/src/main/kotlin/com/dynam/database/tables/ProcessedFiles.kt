package com.dynam.database.tables

import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ProcessedFiles : Table() {
    val id = integer("id").autoIncrement()
    val filePath = text("file_path")
    val hash = text("hash")
    val processedAt = datetime("processed_at").clientDefault { java.time.LocalDateTime.now() }
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(filePath)
    }
}