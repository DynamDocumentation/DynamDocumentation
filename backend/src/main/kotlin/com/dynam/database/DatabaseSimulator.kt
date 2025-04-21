// src/main/kotlin/com/dynam/database/DatabaseSimulator.kt
// src/main/kotlin/com/dynam/database/DatabaseSimulator.kt
package com.dynam.database
import com.dynam.models.User

import kotlinx.serialization.Serializable

@Serializable
class DatabaseSimulator {
    fun fetchUsers(): List<User> = listOf(
        User(1, "João Silva", "joao@example.com"),
        User(2, "Maria Souza", "maria@example.com"),
        User(3, "Carlos Oliveira", "carlos@example.com")
    )
}
