package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Namespaces
import com.dynam.dtos.table.Namespace
import org.jetbrains.exposed.sql.*

class NamespaceRepository {
    private fun fromRow(row: ResultRow) = Namespace(
        id = row[Namespaces.id],
        name = row[Namespaces.name]
    )
    
    suspend fun getAll(): List<Namespace> = dbQuery {
        Namespaces.selectAll().map { fromRow(it) }
    }
    
    suspend fun getById(id: Int): Namespace? = dbQuery {
        Namespaces.selectAll()
            .where { Namespaces.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun getByName(name: String): Namespace? = dbQuery {
        Namespaces.selectAll()
            .where { Namespaces.name eq name }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun getByLibrary(libraryName: String): List<Namespace> = dbQuery {
        Namespaces.selectAll()
            .where { Namespaces.name.lowerCase() like "%${libraryName.lowercase()}%" }
            .map { fromRow(it) }
    }
    
    suspend fun getAllLibraryNames(): List<String> = dbQuery {
        val allNamespaces = Namespaces.selectAll().map { it[Namespaces.name] }
        
        allNamespaces
            .map { namespace -> namespace.split('.').firstOrNull() ?: namespace }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }
    
    suspend fun create(name: String): Namespace = dbQuery {
        val id = Namespaces.insert {
            it[Namespaces.name] = name
        } get Namespaces.id
        
        Namespace(id, name)
    }
    
    suspend fun getOrCreate(name: String): Namespace {
        return getByName(name) ?: create(name)
    }
}
