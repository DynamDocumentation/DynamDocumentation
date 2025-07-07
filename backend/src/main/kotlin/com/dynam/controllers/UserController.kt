package com.dynam.controllers

import com.dynam.dtos.ApiResponses
import com.dynam.dtos.AuthToken
import com.dynam.dtos.LoginRequest
import com.dynam.dtos.LoginResponse
import com.dynam.dtos.table.User
import com.dynam.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.security.MessageDigest
import java.util.*

class UserController(private val userRepository: UserRepository = UserRepository()) {
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }

    suspend fun listUsers(call: ApplicationCall) {
        call.application.log.info("Listing all users")
        val users = userRepository.getAll()
        call.respond(
            HttpStatusCode.OK,
            ApiResponses.success(
                data = users,
                message = "Retrieved ${users.size} users"
            )
        )
    }

    suspend fun registerUser(call: ApplicationCall) {
        try {
            call.application.log.info("Processing user registration")
            val user = call.receive<User>()
            if (user.username.isBlank() || user.email.isBlank() || user.password.isBlank()) {
                call.application.log.warn("Registration rejected: Missing required fields")
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponses.error("Username, email, and password are required")
                )
                return
            }
            val existingUsername = userRepository.getByUsername(user.username)
            if (existingUsername != null) {
                call.application.log.warn("Registration rejected: Username ${user.username} already exists")
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponses.error("Username already exists")
                )
                return
            }
            val existingEmail = userRepository.getByEmail(user.email)
            if (existingEmail != null) {
                call.application.log.warn("Registration rejected: Email ${user.email} already exists")
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponses.error("Email already exists")
                )
                return
            }
            val passwordHash = hashPassword(user.password)
            call.application.log.info("Creating new user: ${user.username}")
            val createdUser = userRepository.create(user, passwordHash)
            if (createdUser == null) {
                call.application.log.error("Failed to create user: ${user.username}")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponses.error("Failed to create user")
                )
                return
            }
            call.respond(
                HttpStatusCode.Created,
                ApiResponses.success(
                    data = createdUser,
                    message = "User registered successfully"
                )
            )
        } catch (e: Exception) {
            call.application.log.error("Error during registration: ${e.message}")
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponses.error("Server error: ${e.message}")
            )
        }
    }

    suspend fun deleteUser(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.application.log.warn("Invalid user ID format for deletion")
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponses.error("Invalid ID format")
            )
            return
        }
        call.application.log.info("Attempting to delete user with ID: $id")
        val deleted = userRepository.delete(id)
        if (deleted) {
            call.application.log.info("User with ID $id deleted successfully")
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.application.log.warn("User deletion failed: ID $id not found")
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponses.error("User not found")
            )
        }
    }

    suspend fun loginUser(call: ApplicationCall) {
        try {
            val loginRequest = call.receive<LoginRequest>()
            if (loginRequest.email.isNullOrBlank() || loginRequest.password.isNullOrBlank()) {
                call.application.log.warn("Login rejected: Missing required fields")
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponses.error("Email and password are required")
                )
                return
            }
            val passwordHash = hashPassword(loginRequest.password)
            val user = userRepository.verifyCredentials(loginRequest.email, passwordHash)
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponses.error("Invalid username or password")
                )
                return
            }
            user.id?.let { userRepository.updateLastLogin(it) }
            val timestamp = System.currentTimeMillis()
            val authToken = Base64.getEncoder().encodeToString(
                "${user.id}:$timestamp:${user.email}".toByteArray()
            )
            val responseData = LoginResponse(
                user = user,
                authToken = authToken
            )
            call.respond(
                HttpStatusCode.OK,
                ApiResponses.success(
                    data = responseData,
                    message = "Login successful"
                )
            )
        } catch (e: Exception) {
            call.application.log.error("Error during user login: ${'$'}{e.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponses.error(e.localizedMessage)
            )
        }
    }

    suspend fun validateAuth(call: ApplicationCall) {
        val request = call.receive<AuthToken>()
        val authToken = request.token
        if (authToken.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponses.error("Auth token is required")
            )
            return
        }
        try {
            val tokenData = String(Base64.getDecoder().decode(authToken)).split(":")
            if (tokenData.size != 3) {
                throw IllegalArgumentException("Invalid token format")
            }
            val userId = tokenData[0].toInt()
            val timestamp = tokenData[1].toLong()
            val email = tokenData[2]
            val currentTime = System.currentTimeMillis()
            val tokenAge = currentTime - timestamp
            val tokenValid = tokenAge < 24 * 60 * 60 * 1000 // 24 hours in milliseconds
            if (!tokenValid) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponses.error("Auth token expired")
                )
                return
            }
            val user = userRepository.getById(userId)
            if (user == null || user.email != email) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponses.error("Invalid auth token")
                )
                return
            }
            call.respond(
                HttpStatusCode.OK,
                ApiResponses.success(
                    data = user,
                    message = "Auth token is valid"
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponses.error("Invalid auth token format")
            )
        }
    }
}
