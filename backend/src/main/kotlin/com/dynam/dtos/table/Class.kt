package com.dynam.dtos.table

import kotlinx.serialization.Serializable

/**
 * Represents a class in the documentation system.
 */
@Serializable
data class Class(
    val id: Int,
    val namespaceId: Int,
    val name: String,
    val description: String?,
    val signature: String?,
    val returnType: String?,
    val example: String?
)
