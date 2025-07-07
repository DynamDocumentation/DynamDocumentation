package com.dynam.repositories

import com.dynam.config.dbQuery
import com.dynam.database.tables.Users
import com.dynam.dtos.table.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import kotlin.jvm.Throws

class UserRepository {
    private fun fromRow(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        email = row[Users.email],
        createdAt = row[Users.createdAt],
        lastLogin = row[Users.lastLogin]
        // Note: passwordHash is deliberately not included in the DTO
        // as we don't want to expose it in API responses
    )
    
    suspend fun getAll(): List<User> = dbQuery {
        Users.selectAll().map { fromRow(it) }
    }
    
    suspend fun getById(id: Int): User? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun getByUsername(username: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.username eq username }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun getByEmail(email: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .map { fromRow(it) }
            .singleOrNull()
    }
    
    suspend fun create(user: User, passwordHash: String): User? = dbQuery {
        try {
            val currentTime = Instant.now().epochSecond
            val id = Users.insert {
                it[username] = user.username
                it[email] = user.email
                it[Users.passwordHash] = passwordHash
                it[createdAt] = currentTime
                it[lastLogin] = currentTime
            } get Users.id
            
            println("Successfully created user with ID: ${id}")
            
            val newUser = Users.selectAll()
                .where { Users.id eq id }
                .map { fromRow(it) }
                .singleOrNull()
                
            println("Successfully: ${newUser}")
            return@dbQuery newUser
        } catch (e: Exception) {
            println("Error creating user: ${e.message}")
            e.printStackTrace()
            return@dbQuery null
        }
    }
    
    suspend fun updateLastLogin(id: Int): Boolean = dbQuery {
        val currentTime = Instant.now().epochSecond
        Users.update({ Users.id eq id }) {
            it[lastLogin] = currentTime
        } > 0
    }
    
    suspend fun delete(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
    
    suspend fun verifyCredentials(email: String, password: String): User? = dbQuery {
        val user = Users.selectAll()
            .where { Users.email eq email }
            .singleOrNull()
        
        if (user != null) {
            val storedHash = user[Users.passwordHash]
            return@dbQuery if (storedHash == password) {
                fromRow(user)
            } else {
                null
            }
        } else {
            null
        }
    }
}
