package com.dynam.dtos.table

import kotlinx.serialization.Serializable

/**
 * Represents a user in the system.
 */
@Serializable
data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val password: String = "",
    val createdAt: Long = 0,
    val lastLogin: Long? = null
)
