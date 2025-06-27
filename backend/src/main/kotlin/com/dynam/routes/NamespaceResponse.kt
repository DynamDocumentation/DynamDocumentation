package com.dynam.routes

import kotlinx.serialization.Serializable
import com.dynam.utils.*

@Serializable
data class NamespaceResponse(
    val namespace: Namespace,
    val children: List<String>
)