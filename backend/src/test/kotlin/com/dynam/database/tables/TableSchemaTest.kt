package com.dynam.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class TableSchemaTest {
    private lateinit var db: Database

    @Before
    fun setUp() {
        db = Database.connect("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
    }

    @Test
    fun `Users table has expected columns and constraints`() {
        transaction(db) {
            SchemaUtils.create(Users)
            val columns = Users.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("username" in columns)
            assertTrue("email" in columns)
            assertTrue("password_hash" in columns)
            assertTrue("created_at" in columns)
            assertTrue("last_login" in columns)
            // Check unique indexes
            assertTrue(Users.indices.any { it.columns.any { c -> c.name == "username" } && it.unique })
            assertTrue(Users.indices.any { it.columns.any { c -> c.name == "email" } && it.unique })
        }
    }

    @Test
    fun `Classes table has expected columns and constraints`() {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(Namespaces, Classes)
            val columns = Classes.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("namespace_id" in columns)
            assertTrue("name" in columns)
            assertTrue("description" in columns)
            assertTrue("signature" in columns)
            assertTrue("return_type" in columns)
            assertTrue("example" in columns)
        }
    }

    @Test
    fun `Variables table has expected columns and constraints`() {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(Classes, Functions, Variables)
            val columns = Variables.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("class_id" in columns)
            assertTrue("function_id" in columns)
            assertTrue("type" in columns)
            assertTrue("name" in columns)
            assertTrue("data_type" in columns)
            assertTrue("description" in columns)
            assertTrue("default_value" in columns)
        }
    }

    @Test
    fun `Functions table has expected columns and constraints`() {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(Classes, Namespaces, Functions)
            val columns = Functions.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("parent_class_id" in columns)
            assertTrue("parent_namespace_id" in columns)
            assertTrue("name" in columns)
            assertTrue("signature" in columns)
            assertTrue("description" in columns)
            assertTrue("return_type" in columns)
            assertTrue("example" in columns)
        }
    }

    @Test
    fun `Namespaces table has expected columns`() {
        transaction(db) {
            SchemaUtils.create(Namespaces)
            val columns = Namespaces.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("name" in columns)
        }
    }

    @Test
    fun `LibraryRequests table has expected columns`() {
        transaction(db) {
            SchemaUtils.create(LibraryRequests)
            val columns = LibraryRequests.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("name" in columns)
            assertTrue("accepted" in columns)
        }
    }

    @Test
    fun `ProcessedFiles table has expected columns and unique index`() {
        transaction(db) {
            SchemaUtils.create(ProcessedFiles)
            val columns = ProcessedFiles.columns.map { it.name }
            assertTrue("id" in columns)
            assertTrue("file_path" in columns)
            assertTrue("hash" in columns)
            assertTrue("processed_at" in columns)
            // Check unique index on file_path
            assertTrue(ProcessedFiles.indices.any { it.columns.any { c -> c.name == "file_path" } && it.unique })
        }
    }
}
