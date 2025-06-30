package com.dynam.models

import kotlinx.serialization.Serializable

/**
 * Represents a constant in the documentation system.
 * A constant is a named value associated with an entity.
 */
@Serializable
data class Constant(
    val id: Int,
    val entityId: Int,
    val name: String,
    val value: String
)
