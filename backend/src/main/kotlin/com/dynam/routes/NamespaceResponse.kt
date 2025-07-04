package com.dynam.routes

import kotlinx.serialization.Serializable
import com.dynam.utils.*
import com.dynam.models.Namespace

@Serializable
data class NamespaceResponse(
    val namespace: Namespace,
    val children: List<String>
)