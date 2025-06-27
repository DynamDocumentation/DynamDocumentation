package com.dynam.enums

import kotlinx.serialization.Serializable

@Serializable
enum class VariableType {
    DESCRIPTION, NAME, PARAMETER, SIGNATURE, RETURN, RAISES, NOTES, EXAMPLES, TYPE
}