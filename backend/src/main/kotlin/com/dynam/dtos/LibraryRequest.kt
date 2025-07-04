package com.dynam.dtos

import kotlinx.serialization.Serializable

/**
 * Represents a library request in the documentation system.
 * A library request is used to track requests for new libraries to be added.
 */
@Serializable
data class LibraryRequest(
    val id: Int,
    val name: String,
    val accepted: Boolean
)
