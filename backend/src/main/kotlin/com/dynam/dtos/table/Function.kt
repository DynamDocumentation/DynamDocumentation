package com.dynam.dtos.table

import kotlinx.serialization.Serializable

/**
 * Represents a function in the documentation system.
 */
@Serializable
data class Function(
    val id: Int,
    val parentClassId: Int?,
    val parentNamespaceId: Int?,
    val name: String,
    val signature: String?,
    val description: String?,
    val returnType: String?,
    val example: String?
)
