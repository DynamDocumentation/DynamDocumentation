package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.LibraryRequests
import com.dynam.dtos.table.LibraryRequest
import org.jetbrains.exposed.sql.*

/**
 * Repository for LibraryRequest-related database operations.
 * This class handles all database access for LibraryRequest objects.
 */
class LibraryRequestRepository {
    /**
     * Convert a database row to a LibraryRequest object
     */
    private fun fromRow(row: ResultRow) = LibraryRequest(
        id = row[LibraryRequests.id],
        name = row[LibraryRequests.name],
        accepted = row[LibraryRequests.accepted]
    )
    
    /**
     * Get all library requests from the database
     */
    suspend fun getAll(): List<LibraryRequest> = dbQuery {
        try {
            LibraryRequests.selectAll().map { fromRow(it) }
        } catch (e: Exception) {
            println("Error fetching library requests: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get a library request by its ID
     */
    suspend fun getById(id: Int): LibraryRequest? = dbQuery {
        LibraryRequests.selectAll()
            .where { LibraryRequests.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Get a library request by its name
     */
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
    
    /**
     * Get all accepted or pending library requests
     */
    suspend fun getByAcceptedStatus(accepted: Boolean): List<LibraryRequest> = dbQuery {
        LibraryRequests.selectAll()
            .where { LibraryRequests.accepted eq accepted }
            .map { fromRow(it) }
    }
    
    /**
     * Create a new library request
     */
    suspend fun create(name: String): LibraryRequest = dbQuery {
        val id = LibraryRequests.insert {
            it[LibraryRequests.name] = name
            it[LibraryRequests.accepted] = false // Default to not accepted
        } get LibraryRequests.id
        
        LibraryRequest(id, name, false)
    }
    
    /**
     * Update the acceptance status of a library request
     */
    suspend fun updateAcceptanceStatus(id: Int, accepted: Boolean): Boolean = dbQuery {
        val updatedRows = LibraryRequests.update({ LibraryRequests.id eq id }) {
            it[LibraryRequests.accepted] = accepted
        }
        updatedRows > 0
    }
    
    // /**
    //  * Delete a library request
    //  */
    // suspend fun delete(id: Int): Boolean = dbQuery {
    //     val deletedRows = LibraryRequests.deleteWhere { LibraryRequests.id eq id }
    //     deletedRows > 0
    // }
}
