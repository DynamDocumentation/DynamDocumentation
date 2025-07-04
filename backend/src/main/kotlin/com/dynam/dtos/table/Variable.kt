package com.dynam.dtos.table

import com.dynam.enums.VariableType
import kotlinx.serialization.Serializable

/**
 * Represents a variable in the documentation system.
 * A variable can be a parameter, return value, or description.
 */
@Serializable
data class Variable(
    val id: Int,
    val entityId: Int,
    val type: VariableType,
    val name: String,
    val description: String
)
