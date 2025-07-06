package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.LibraryRequests
import com.dynam.dtos.table.LibraryRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class LibraryRequestRepository {
    private fun fromRow(row: ResultRow) = LibraryRequest(
        id = row[LibraryRequests.id],
        name = row[LibraryRequests.name],
        accepted = row[LibraryRequests.accepted]
    )
    
    suspend fun getAll(): List<LibraryRequest> = dbQuery {
        try {
            LibraryRequests.selectAll().map { fromRow(it) }
        } catch (e: Exception) {
            println("Error fetching library requests: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getById(id: Int): LibraryRequest? = dbQuery {
        LibraryRequests.selectAll()
            .where { LibraryRequests.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun getByName(name: String): LibraryRequest? = dbQuery {
        try {
            LibraryRequests.selectAll()
                .where { LibraryRequests.name eq name }
                .map { fromRow(it) }
                .singleOrNull()
        } catch (e: Exception) {
            println("Error fetching library request by name: ${e.message}")
            null
        }
    }
    
    suspend fun getByAcceptedStatus(accepted: Boolean): List<LibraryRequest> = dbQuery {
        LibraryRequests.selectAll()
            .where { LibraryRequests.accepted eq accepted }
            .map { fromRow(it) }
    }
    
    suspend fun create(name: String): LibraryRequest = dbQuery {
        val id = LibraryRequests.insert {
            it[LibraryRequests.name] = name
            it[LibraryRequests.accepted] = false
        } get LibraryRequests.id
        
        LibraryRequest(id, name, false)
    }
    
    suspend fun updateAcceptanceStatus(id: Int, accepted: Boolean): Boolean = dbQuery {
        val updatedRows = LibraryRequests.update({ LibraryRequests.id eq id }) {
            it[LibraryRequests.accepted] = accepted
        }
        updatedRows > 0
    }
    
    suspend fun delete(id: Int): Boolean = dbQuery {
        val deletedRows = LibraryRequests.deleteWhere { LibraryRequests.id eq id }
        deletedRows > 0
    }
}
