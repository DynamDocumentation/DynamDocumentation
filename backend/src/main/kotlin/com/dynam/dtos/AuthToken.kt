package com.dynam.dtos

import kotlinx.serialization.Serializable

/**
 * Data class representing an authentication response
 */
@Serializable
data class AuthToken(
    val token: String
)
