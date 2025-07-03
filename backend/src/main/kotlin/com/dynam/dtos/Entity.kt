package com.dynam.dtos

import com.dynam.enums.EntityType
import kotlinx.serialization.Serializable

/**
 * Represents an entity in the documentation system.
 * An entity can be a class, function, or other code element.
 */
@Serializable
data class Entity(
    val id: Int, 
    val type: EntityType, 
    val name: String,
    val description: String,
    val namespaceId: Int
)
