package com.dynam.database.tables

import org.jetbrains.exposed.sql.*

object Users: Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = long("created_at")
    val lastLogin = long("last_login").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}
