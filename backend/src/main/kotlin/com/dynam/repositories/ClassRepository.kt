package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Classes
import com.dynam.dtos.table.Class
import org.jetbrains.exposed.sql.*

class ClassRepository {
    private fun fromRow(row: ResultRow) = Class(
        id = row[Classes.id],
        namespaceId = row[Classes.namespaceId],
        name = row[Classes.name],
        description = row[Classes.description],
        signature = row[Classes.signature],
        returnType = row[Classes.returnType],
        example = row[Classes.example]
    )
    
    suspend fun getAll(): List<Class> = dbQuery {
        Classes.selectAll().map { fromRow(it) }
    }
    
    suspend fun getById(id: Int): Class? = dbQuery {
        Classes.selectAll()
            .where { Classes.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }

    suspend fun getByNamespace(namespaceId: Int): List<Class> = dbQuery {
        Classes.selectAll()
            .where { Classes.namespaceId eq namespaceId }
            .map { fromRow(it) }
    }
    
    suspend fun getByLibrary(libraryName: String): List<Class> = dbQuery {
        val pattern = "%${libraryName.lowercase()}%"
        
        (Classes innerJoin com.dynam.database.tables.Namespaces)
            .selectAll()
            .where { com.dynam.database.tables.Namespaces.name.lowerCase() like pattern }
            .map { fromRow(it) }
    }
}
