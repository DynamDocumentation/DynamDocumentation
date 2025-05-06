package com.dynam.utils

import kotlinx.serialization.Serializable

@Serializable
object EntityType {
    public val CLASS = "class"
    public val FUNCTION = "function"
}

@Serializable
data class Namespace(
    val id: Int,
    val name: String
)

@Serializable
data class EntityDetails(
    val type: String,
    val namespace: String,
    val parameters: List<Value>?,
    val attributes: List<Value>?,
)

@Serializable
data class Value(
    val name: String,
    val type: String,
    val defaultValue: String?,
    val description: String
)