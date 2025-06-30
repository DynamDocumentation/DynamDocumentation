package com.dynam.models

import kotlinx.serialization.Serializable

/**
 * Represents a namespace in the documentation system.
 * A namespace is a container for related entities.
 */
@Serializable
data class Namespace(
    val id: Int,
    val name: String
)
