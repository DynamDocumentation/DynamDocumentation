package com.dynam.dtos

import kotlinx.serialization.Serializable

/**
 * A standardized API response wrapper.
 * Used to provide consistent API responses across the application.
 */
@Serializable
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null
)

/**
 * Factory methods to create standard API responses.
 */
object ApiResponses {
    /**
     * Create a success response.
     */
    fun <T> success(data: T? = null, message: String? = null): ApiResponse<T> {
        return ApiResponse(
            status = "success",
            message = message,
            data = data
        )
    }

    /**
     * Create an error response.
     */
    fun error(message: String): ApiResponse<Nothing> {
        return ApiResponse(
            status = "error",
            message = message,
            data = null
        )
    }
}
