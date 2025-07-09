package com.dynam.dtos.response

import com.dynam.dtos.table.Function
import com.dynam.dtos.table.Variable
import kotlinx.serialization.Serializable

@Serializable
data class FunctionResponse(
    val entity: Function,
    val attributes: List<Variable>,
    val parameters: List<Variable>,
    val returns: List<Variable>
)
