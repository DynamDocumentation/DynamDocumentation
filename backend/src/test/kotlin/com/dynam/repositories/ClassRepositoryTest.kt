package com.dynam.repositories

import com.dynam.DatabaseTest
import com.dynam.database.tables.Classes
import com.dynam.database.tables.Namespaces
import com.dynam.dtos.table.Class
import org.jetbrains.exposed.sql.insert
import kotlin.test.*

class ClassRepositoryTest : DatabaseTest() {
    private lateinit var repo: ClassRepository
    private var namespaceId: Int = 0

    @BeforeTest
    fun setUpRepo() {
        repo = ClassRepository()
        // Insert a namespace for foreign key
        namespaceId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert {
                it[name] = "test.lib"
            } get Namespaces.id
        }
    }

    @Test
    fun testCreateAndGetById() = runTest {
        val classId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[namespaceId] = this@ClassRepositoryTest.namespaceId
                it[name] = "TestClass"
                it[description] = "A test class"
                it[signature] = "TestClass()"
                it[returnType] = "TestType"
                it[example] = "example usage"
            } get Classes.id
        }
        val found = repo.getById(classId)
        assertNotNull(found)
        assertEquals("TestClass", found.name)
    }

    @Test
    fun testGetAll() = runTest {
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[namespaceId] = this@ClassRepositoryTest.namespaceId
                it[name] = "ClassA"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            }
            Classes.insert {
                it[namespaceId] = this@ClassRepositoryTest.namespaceId
                it[name] = "ClassB"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val all = repo.getAll()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.name == "ClassA" })
        assertTrue(all.any { it.name == "ClassB" })
    }

    @Test
    fun testGetByNamespace() = runTest {
        val otherNamespaceId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert { it[name] = "other.lib" } get Namespaces.id
        }
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[namespaceId] = this@ClassRepositoryTest.namespaceId
                it[name] = "ClassInMain"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            }
            Classes.insert {
                it[namespaceId] = otherNamespaceId
                it[name] = "ClassInOther"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val found = repo.getByNamespace(namespaceId)
        assertTrue(found.any { it.name == "ClassInMain" })
        assertTrue(found.none { it.name == "ClassInOther" })
    }

    @Test
    fun testGetByLibrary() = runTest {
        val nsId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert { it[name] = "libX.special" } get Namespaces.id
        }
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[namespaceId] = nsId
                it[name] = "ClassLibX"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val found = repo.getByLibrary("libX")
        assertTrue(found.any { it.name == "ClassLibX" })
    }
}
