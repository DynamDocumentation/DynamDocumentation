package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Functions
import com.dynam.dtos.table.Function
import org.jetbrains.exposed.sql.*

class FunctionRepository {
    private fun fromRow(row: ResultRow) = Function(
        id = row[Functions.id],
        parentClassId = row[Functions.parentClassId],
        parentNamespaceId = row[Functions.parentNamespaceId],
        name = row[Functions.name],
        signature = row[Functions.signature],
        description = row[Functions.description],
        returnType = row[Functions.returnType],
        example = row[Functions.example]
    )
    
    suspend fun getAll(): List<Function> = dbQuery {
        Functions.selectAll().map { fromRow(it) }
    }
    
    suspend fun getById(id: Int): Function? = dbQuery {
        Functions.selectAll()
            .where { Functions.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }

    suspend fun getByNamespace(namespaceId: Int): List<Function> = dbQuery {
        Functions.selectAll()
            .where { 
                (Functions.parentNamespaceId eq namespaceId) or
                (Functions.parentClassId inSubQuery 
                    com.dynam.database.tables.Classes
                        .slice(com.dynam.database.tables.Classes.id)
                        .select { com.dynam.database.tables.Classes.namespaceId eq namespaceId }
                )
            }
            .map { fromRow(it) }
    }
    
    suspend fun getDirectNamespaceFunctions(namespaceId: Int): List<Function> = dbQuery {
        Functions.selectAll()
            .where { Functions.parentNamespaceId eq namespaceId }
            .map { fromRow(it) }
    }
    
    suspend fun getByClass(classId: Int): List<Function> = dbQuery {
        Functions.selectAll()
            .where { Functions.parentClassId eq classId }
            .map { fromRow(it) }
    }
    
    suspend fun getByLibrary(libraryName: String): List<Function> = dbQuery {
        val pattern = "%${libraryName.lowercase()}%"
        
        (Functions innerJoin com.dynam.database.tables.Namespaces)
            .selectAll()
            .where { com.dynam.database.tables.Namespaces.name.lowerCase() like pattern }
            .map { fromRow(it) }
    }
}
