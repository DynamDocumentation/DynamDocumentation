package com.dynam.database.tables

import org.jetbrains.exposed.sql.*

object LibraryRequests: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val accepted = bool("accepted")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}