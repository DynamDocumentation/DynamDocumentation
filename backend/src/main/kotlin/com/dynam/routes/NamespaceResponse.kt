package com.dynam.routes

import kotlinx.serialization.Serializable

@Serializable
data class NamespaceResponse(
    val namespace: String,
    val children: Array<String>
)