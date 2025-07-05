package com.dynam.database.tables

import org.jetbrains.exposed.sql.*

object Users: Table() {
    val id = integer("id").autoIncrement()
    val username = text("username").uniqueIndex()
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val createdAt = long("created_at")
    val lastLogin = long("last_login").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}
