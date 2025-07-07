package com.dynam.database.tables

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class TablesMigrationTest {
    private lateinit var db: Database

    @Before
    fun setUp() {
        db = Database.connect("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
        transaction(db) {
            SchemaUtils.create(
                Users, Classes, Variables, Functions, Namespaces, LibraryRequests, ProcessedFiles
            )
        }
    }

    @After
    fun tearDown() {
        transaction(db) {
            SchemaUtils.drop(
                Users, Classes, Variables, Functions, Namespaces, LibraryRequests, ProcessedFiles
            )
        }
    }

    @Test
    fun `all tables should be created successfully`() {
        transaction(db) {
            // Try a selectAll on each table to verify creation
            assertTrue(Users.selectAll().toList() != null)
            assertTrue(Classes.selectAll().toList() != null)
            assertTrue(Variables.selectAll().toList() != null)
            assertTrue(Functions.selectAll().toList() != null)
            assertTrue(Namespaces.selectAll().toList() != null)
            assertTrue(LibraryRequests.selectAll().toList() != null)
            assertTrue(ProcessedFiles.selectAll().toList() != null)
        }
    }

    @Test
    fun `dropping tables should not throw`() {
        transaction(db) {
            SchemaUtils.drop(Users, Classes, Variables, Functions, Namespaces, LibraryRequests, ProcessedFiles)
        }
    }

    @Test
    fun `tables can be recreated after drop`() {
        transaction(db) {
            SchemaUtils.drop(Users, Classes, Variables, Functions, Namespaces, LibraryRequests, ProcessedFiles)
            SchemaUtils.create(Users, Classes, Variables, Functions, Namespaces, LibraryRequests, ProcessedFiles)
            assertTrue(Users.selectAll().toList() != null)
            assertTrue(Classes.selectAll().toList() != null)
            assertTrue(Variables.selectAll().toList() != null)
            assertTrue(Functions.selectAll().toList() != null)
            assertTrue(Namespaces.selectAll().toList() != null)
            assertTrue(LibraryRequests.selectAll().toList() != null)
            assertTrue(ProcessedFiles.selectAll().toList() != null)
        }
    }
}
