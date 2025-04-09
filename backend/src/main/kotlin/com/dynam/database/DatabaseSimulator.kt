// src/main/kotlin/com/dynam/database/DatabaseSimulator.kt
package com.dynam.database

import com.dynam.models.User

class DatabaseSimulator {
    fun fetchUsers(): List<User> = listOf(
        User(1, "João", "joao@email.com"),
        User(2, "Maria", "maria@email.com"),
        User(3, "José", "jose@email.com")
    )
}

