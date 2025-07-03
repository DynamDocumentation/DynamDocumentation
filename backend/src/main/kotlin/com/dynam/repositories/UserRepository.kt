package com.dynam.repositories

import com.dynam.database.dbQuery
import com.dynam.database.tables.Users
import com.dynam.dtos.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import kotlin.jvm.Throws

/**
 * Repository for User-related database operations.
 * This class handles all database access for User objects.
 */
class UserRepository {
    /**
     * Convert a database row to a User object
     */
    private fun fromRow(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        email = row[Users.email],
        createdAt = row[Users.createdAt],
        lastLogin = row[Users.lastLogin]
        // Note: passwordHash is deliberately not included in the DTO
        // as we don't want to expose it in API responses
    )
    
    /**
     * Get all users from the database
     */
    suspend fun getAll(): List<User> = dbQuery {
        Users.selectAll().map { fromRow(it) }
    }
    
    /**
     * Get a user by their ID
     */
    suspend fun getById(id: Int): User? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Get a user by their username
     */
    suspend fun getByUsername(username: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.username eq username }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    /**
     * Create a new user in the database
     */
    suspend fun create(user: User, passwordHash: String): User? = dbQuery {
        try {
            val currentTime = Instant.now().epochSecond
            val id = Users.insert {
                it[username] = user.username
                it[email] = user.email
                it[Users.passwordHash] = passwordHash
                it[createdAt] = currentTime
                it[lastLogin] = currentTime  // Set lastLogin to creation time for new users
            } get Users.id
            
            getById(id)
        } catch (e: Exception) {
            println("Error creating user: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Update user's last login time
     */
    suspend fun updateLastLogin(id: Int): Boolean = dbQuery {
        val currentTime = Instant.now().epochSecond
        Users.update({ Users.id eq id }) {
            it[lastLogin] = currentTime
        } > 0
    }
    
    /**
     * Delete a user by their ID
     */
    suspend fun delete(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
