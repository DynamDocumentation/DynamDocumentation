package com.dynam.dtos.response

import com.dynam.dtos.table.Class
import com.dynam.dtos.table.Variable
import kotlinx.serialization.Serializable

@Serializable
data class ClassResponse(
    val entity: Class,
    val attributes: List<Variable>,
    val parameters: List<Variable>,
    val returns: List<Variable>
)
