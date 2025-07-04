package com.dynam.dtos

import kotlinx.serialization.Serializable
import com.dynam.dtos.table.User

/**
 * Data class representing a login response with user details and authentication token
 */
@Serializable
data class LoginResponse(
    val user: User,
    val authToken: String
)
