package com.dynam.routes

import com.dynam.dtos.ApiResponse
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
import io.ktor.server.routing.*
import java.security.MessageDigest
import java.util.*

class UserRoutes {
    private val userRepository = UserRepository()
    
    // Simple password hashing function - in a real app, use a proper hashing library
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    fun registerRoutes(route: Route) {
        route.route("/api/users") {
            /**
             * GET /api/users
             * 
             * List all users in the system.
             * Returns an array of user objects (without passwords).
             */
            get("/list") {
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
            
            /**
             * GET /api/users/{id}
             * 
             * Get details for a specific user by their ID.
             * Returns a user object if found (without password).
             */
            get("/details/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.application.log.warn("Invalid user ID format")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponses.error("Invalid ID format")
                    )
                    return@get
                }
                
                call.application.log.info("Getting user details for ID: $id")
                val user = userRepository.getById(id)
                if (user == null) {
                    call.application.log.warn("User not found with ID: $id")
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponses.error("User not found")
                    )
                    return@get
                }
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponses.success(data = user)
                )
            }
            
            /**
             * POST /api/users/register
             * 
             * Register a new user in the system.
             * Requires username, email, and password in the request body.
             * Returns the created user object (without password).
             */
            post("/register") {
                try {
                    call.application.log.info("Processing user registration")
                    val user = call.receive<User>()
                    
                    // Validate input
                    if (user.username.isBlank() || user.email.isBlank() || user.password.isBlank()) {
                        call.application.log.warn("Registration rejected: Missing required fields")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponses.error("Username, email, and password are required")
                        )
                        return@post
                    }
                    
                    // Check if username exists
                    val existingUsername = userRepository.getByUsername(user.username)
                    if (existingUsername != null) {
                        call.application.log.warn("Registration rejected: Username ${user.username} already exists")
                        call.respond(
                            HttpStatusCode.Conflict,
                            ApiResponses.error("Username already exists")
                        )
                        return@post
                    }
                    
                    // Check if email exists
                    val existingEmail = userRepository.getByEmail(user.email)
                    if (existingEmail != null) {
                        call.application.log.warn("Registration rejected: Email ${user.email} already exists")
                        call.respond(
                            HttpStatusCode.Conflict,
                            ApiResponses.error("Email already exists")
                        )
                        return@post
                    }
                    
                    // Hash the password
                    val passwordHash = hashPassword(user.password)
                    
                    // Create the user
                    call.application.log.info("Creating new user: ${user.username}")
                    val createdUser = userRepository.create(user, passwordHash)
                    if (createdUser == null) {
                        call.application.log.error("Failed to create user: ${user.username}")
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponses.error("Failed to create user")
                        )
                        return@post
                    }
                    
                    call.application.log.info("User registered successfully: ${user.username}")
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponses.success(
                            data = createdUser,
                            message = "User registered successfully"
                        )
                    )
                } catch (e: Exception) {
                    call.application.log.error("Error during user registration: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponses.error(e.localizedMessage)
                    )
                }
            }
            
            /**
             * DELETE /api/users/{id}
             * 
             * Remove a user from the system by their ID.
             * Returns a 204 No Content on success.
             */
            delete("/remove/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.application.log.warn("Invalid user ID format for deletion")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponses.error("Invalid ID format")
                    )
                    return@delete
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
            
            /**
             * POST /api/users/login
             * 
             * Authenticate a user and return their details.
             * Requires username and password in the request body.
             * Returns the user object (without password) on successful login.
             */
            post("/login") {
                try {
                    val loginRequest = call.receive<LoginRequest>()
                    
                    // Validate input
                    if (loginRequest.email.isBlank() || loginRequest.password.isBlank()) {
                        call.application.log.warn("Login rejected: Missing required fields")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponses.error("Email and password are required")
                        )
                        return@post
                    }
                    
                    // Hash the password for comparison
                    val passwordHash = hashPassword(loginRequest.password)
                    
                    // Verify credentials
                    val user = userRepository.verifyCredentials(loginRequest.email, passwordHash)
                    
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponses.error("Invalid username or password")
                        )
                        return@post
                    }
                    
                    // Update last login timestamp
                    user.id?.let { userRepository.updateLastLogin(it) }
                    
                    // Generate a simple auth token (userId:timestamp:email)
                    val timestamp = System.currentTimeMillis()
                    val authToken = Base64.getEncoder().encodeToString(
                        "${user.id}:$timestamp:${user.email}".toByteArray()
                    )
                    
                    // Create a response with user and auth token
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
                    call.application.log.error("Error during user login: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponses.error(e.localizedMessage)
                    )
                }
            }
            
            /**
             * POST /api/users/validate-auth
             * 
             * Validates an auth token.
             * Requires authToken in the request body.
             */
            post("/validate-auth") {
                val request = call.receive<AuthToken>()
                val authToken = request.token
                
                if (authToken.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponses.error("Auth token is required")
                    )
                    return@post
                }
                
                try {
                    // Decode and validate the token
                    val tokenData = String(Base64.getDecoder().decode(authToken)).split(":")
                    if (tokenData.size != 3) {
                        throw IllegalArgumentException("Invalid token format")
                    }
                    
                    val userId = tokenData[0].toInt()
                    val timestamp = tokenData[1].toLong()
                    val email = tokenData[2]
                    
                    // Check if token is expired (24 hours validity)
                    val currentTime = System.currentTimeMillis()
                    val tokenAge = currentTime - timestamp
                    val tokenValid = tokenAge < 24 * 60 * 60 * 1000 // 24 hours in milliseconds
                    
                    if (!tokenValid) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponses.error("Auth token expired")
                        )
                        return@post
                    }
                    
                    // Verify the user exists
                    val user = userRepository.getById(userId)
                    if (user == null || user.email != email) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponses.error("Invalid auth token")
                        )
                        return@post
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
    }
}
