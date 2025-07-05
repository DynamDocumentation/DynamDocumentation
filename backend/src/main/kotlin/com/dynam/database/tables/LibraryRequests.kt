package com.dynam.database.tables

import org.jetbrains.exposed.sql.*

object LibraryRequests: Table() {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val accepted = bool("accepted")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}