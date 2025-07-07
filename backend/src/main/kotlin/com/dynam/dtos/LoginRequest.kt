package com.dynam.dtos

import kotlinx.serialization.Serializable

/**
 * Data class representing a login request.
 */
@Serializable
data class LoginRequest(
    val email: String? = null,
    val password: String? = null
)
