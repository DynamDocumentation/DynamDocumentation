package com.dynam.database.tables

import org.jetbrains.exposed.sql.*

object Namespaces: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}