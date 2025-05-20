package com.dynam.models

import kotlinx.serialization.Serializable

@Serializable
data class Value(
    val name: String,
    val type: String,
    val defaultValue: String?,
    val description: String
)

@Serializable
data class ChildDetails(
    val namespace: String,
    val parameters: List<Value>?,
    val attributes: List<Value>?,
)

interface NamespaceChildInterface {

    fun getFromNamespace(namespace: String) : Array<String>

    fun getDetailsOf(namespace: String?) : ChildDetails
}