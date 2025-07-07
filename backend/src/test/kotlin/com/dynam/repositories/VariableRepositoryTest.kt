package com.dynam.repositories

import com.dynam.DatabaseTest
import com.dynam.database.tables.Classes
import com.dynam.database.tables.Functions
import com.dynam.database.tables.Variables
import com.dynam.database.tables.Namespaces
import com.dynam.enums.VariableType
import org.jetbrains.exposed.sql.insert
import kotlin.test.*

class VariableRepositoryTest : DatabaseTest() {
    private lateinit var repo: VariableRepository
    private var classId: Int = 0
    private var functionId: Int = 0

    @BeforeTest
    fun setUpRepo() {
        repo = VariableRepository()
        val namespaceId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Namespaces.insert { it[name] = "test.lib" } get Namespaces.id
        }
        classId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Classes.insert {
                it[Classes.namespaceId] = namespaceId
                it[Classes.name] = "TestClass"
                it[Classes.description] = "desc"
                it[Classes.signature] = "sig"
                it[Classes.returnType] = "type"
                it[Classes.example] = "ex"
            } get Classes.id
        }
        functionId = org.jetbrains.exposed.sql.transactions.transaction(db) {
            Functions.insert {
                it[Functions.parentClassId] = classId
                it[Functions.parentNamespaceId] = namespaceId
                it[Functions.name] = "testFunc"
                it[Functions.signature] = "sig"
                it[Functions.description] = "desc"
                it[Functions.returnType] = "type"
                it[Functions.example] = "ex"
            } get Functions.id
        }
    }

    @Test
    fun testCreateAndGetById() = runTest {
        val created = repo.create(classId = classId, functionId = functionId, type = VariableType.PARAMETER, name = "varA", dataType = "Int", description = "desc", defaultValue = "0")
        assertNotNull(created)
        val found = repo.getById(created.id!!)
        assertNotNull(found)
        assertEquals("varA", found.name)
    }

    @Test
    fun testGetAll() = runTest {
        repo.create(classId = classId, functionId = functionId, type = VariableType.PARAMETER, name = "varB")
        repo.create(classId = classId, functionId = functionId, type = VariableType.PARAMETER, name = "varC")
        val all = repo.getAll()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.name == "varB" })
        assertTrue(all.any { it.name == "varC" })
    }

    @Test
    fun testGetByClassId() = runTest {
        repo.create(classId = classId, type = VariableType.PARAMETER, name = "classVar")
        val found = repo.getByClassId(classId)
        assertTrue(found.any { it.name == "classVar" })
    }

    @Test
    fun testGetByFunctionId() = runTest {
        repo.create(functionId = functionId, type = VariableType.PARAMETER, name = "funcVar")
        val found = repo.getByFunctionId(functionId)
        assertTrue(found.any { it.name == "funcVar" })
    }

    @Test
    fun testGetByClassIdAndType() = runTest {
        repo.create(classId = classId, type = VariableType.PARAMETER, name = "paramVar")
        repo.create(classId = classId, type = VariableType.RETURN, name = "returnVar")
        val found = repo.getByClassIdAndType(classId, VariableType.PARAMETER)
        assertTrue(found.any { it.name == "paramVar" })
        assertTrue(found.none { it.name == "returnVar" })
    }

    @Test
    fun testGetByFunctionIdAndType() = runTest {
        repo.create(functionId = functionId, type = VariableType.PARAMETER, name = "paramFuncVar")
        repo.create(functionId = functionId, type = VariableType.RETURN, name = "returnFuncVar")
        val found = repo.getByFunctionIdAndType(functionId, VariableType.PARAMETER)
        assertTrue(found.any { it.name == "paramFuncVar" })
        assertTrue(found.none { it.name == "returnFuncVar" })
    }

    @Test
    fun testUpdate() = runTest {
        val created = repo.create(classId = classId, type = VariableType.PARAMETER, name = "toUpdate", dataType = "Int")
        val updated = repo.update(created.id, name = "updatedName", dataType = "String")
        assertTrue(updated)
        val found = repo.getById(created.id!!)
        assertNotNull(found)
        assertEquals("updatedName", found.name)
        assertEquals("String", found.dataType)
    }

    @Test
    fun testDelete() = runTest {
        val created = repo.create(classId = classId, type = VariableType.PARAMETER, name = "toDelete")
        val deleted = repo.delete(created.id)
        assertTrue(deleted)
        val found = repo.getById(created.id!!)
        assertNull(found)
    }
}
