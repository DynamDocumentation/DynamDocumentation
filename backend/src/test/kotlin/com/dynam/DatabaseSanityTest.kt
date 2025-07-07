package com.dynam

import com.dynam.database.tables.Namespaces
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseSanityTest : DatabaseTest() {
    @Test
    fun testInMemoryDatabaseWorks() {
        transaction(db) {
            Namespaces.insert { it[Namespaces.name] = "test_namespace" }
        }
        val count = transaction(db) {
            Namespaces.selectAll().count()
        }
        assertEquals(1, count)
    }
}
