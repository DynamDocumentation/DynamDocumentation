package com.dynam.dtos

import kotlinx.serialization.Serializable

/**
 * Data class representing a login response with user details and authentication token
 */
@Serializable
data class LoginResponse(
    val user: User,
    val authToken: String
)
