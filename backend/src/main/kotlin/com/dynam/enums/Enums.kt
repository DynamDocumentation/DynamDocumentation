// Arquivo: com/dynam/enums/Enums.kt

package com.dynam.enums

import kotlinx.serialization.Serializable

@Serializable
enum class EntityType {
    CLASS,
    FUNCTION
}

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