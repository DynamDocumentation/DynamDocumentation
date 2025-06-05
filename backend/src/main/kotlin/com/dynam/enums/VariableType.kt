package com.dynam.enums

import kotlinx.serialization.Serializable

@Serializable
enum class VariableType {
    ATTRIBUTE, PARAMETER, RETURN
}