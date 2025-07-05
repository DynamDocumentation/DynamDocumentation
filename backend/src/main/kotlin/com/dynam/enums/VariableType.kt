package com.dynam.enums

import kotlinx.serialization.Serializable

@Serializable
enum class VariableType {
    PARAMETER,
    RETURN,
    RAISE,
    DESCRIPTION,
    EXAMPLE,
    ATTRIBUTE,
    NAME,
    SIGNATURE,
    NOTES,
    TYPE
}