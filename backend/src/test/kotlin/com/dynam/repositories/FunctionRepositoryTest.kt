package com.dynam.repositories

import com.dynam.DatabaseTest
import com.dynam.database.tables.Functions
import com.dynam.database.tables.Classes
import com.dynam.database.tables.Namespaces
import com.dynam.dtos.table.Function
import org.jetbrains.exposed.sql.insert
import kotlin.test.*

class FunctionRepositoryTest : DatabaseTest() {
    private lateinit var repo: FunctionRepository
    private var namespaceId: Int = 0
    private var classId: Int = 0

    @BeforeTest
    fun setUpRepo() {
        repo = FunctionRepository()
        // Insert a namespace and class for foreign keys
        namespaceId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert { it[name] = "test.lib" } get Namespaces.id
        }
        classId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[namespaceId] = this@FunctionRepositoryTest.namespaceId
                it[name] = "TestClass"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            } get Classes.id
        }
    }

    @Test
    fun testCreateAndGetById() = runTest {
        val functionId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Functions.insert {
                it[parentClassId] = classId
                it[parentNamespaceId] = namespaceId
                it[name] = "testFunc"
                it[signature] = "testFunc()"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            } get Functions.id
        }
        val found = repo.getById(functionId)
        assertNotNull(found)
        assertEquals("testFunc", found.name)
    }

    @Test
    fun testGetAll() = runTest {
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Functions.insert {
                it[parentClassId] = classId
                it[parentNamespaceId] = namespaceId
                it[name] = "funcA"
                it[signature] = "sig"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            }
            Functions.insert {
                it[parentClassId] = classId
                it[parentNamespaceId] = namespaceId
                it[name] = "funcB"
                it[signature] = "sig"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val all = repo.getAll()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.name == "funcA" })
        assertTrue(all.any { it.name == "funcB" })
    }

    @Test
    fun testGetByNamespace() = runTest {
        val otherNamespaceId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert { it[name] = "other.lib" } get Namespaces.id
        }
        val otherClassId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[namespaceId] = otherNamespaceId
                it[name] = "OtherClass"
                it[description] = "desc"
                it[signature] = "sig"
                it[returnType] = "type"
                it[example] = "ex"
            } get Classes.id
        }
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Functions.insert {
                it[parentClassId] = classId
                it[parentNamespaceId] = namespaceId
                it[name] = "funcInMain"
                it[signature] = "sig"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            }
            Functions.insert {
                it[parentClassId] = otherClassId
                it[parentNamespaceId] = otherNamespaceId
                it[name] = "funcInOther"
                it[signature] = "sig"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val found = repo.getByNamespace(namespaceId)
        assertTrue(found.any { it.name == "funcInMain" })
        assertTrue(found.none { it.name == "funcInOther" })
    }

    @Test
    fun testGetDirectNamespaceFunctions() = runTest {
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Functions.insert {
                it[parentClassId] = classId
                it[parentNamespaceId] = namespaceId
                it[name] = "directFunc"
                it[signature] = "sig"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val found = repo.getDirectNamespaceFunctions(namespaceId)
        assertTrue(found.any { it.name == "directFunc" })
    }

    @Test
    fun testGetByLibrary() = runTest {
        val nsId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert { it[name] = "libY.special" } get Namespaces.id
        }
        org.jetbrains.exposed.sql.transactions.transaction(db) {
            Functions.insert {
                it[parentClassId] = classId
                it[parentNamespaceId] = nsId
                it[name] = "funcLibY"
                it[signature] = "sig"
                it[description] = "desc"
                it[returnType] = "type"
                it[example] = "ex"
            }
        }
        val found = repo.getByLibrary("libY")
        assertTrue(found.any { it.name == "funcLibY" })
    }
}
