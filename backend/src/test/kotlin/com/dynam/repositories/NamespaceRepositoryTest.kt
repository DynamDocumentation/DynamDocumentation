package com.dynam.repositories

import com.dynam.DatabaseTest
import com.dynam.database.tables.Namespaces
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class NamespaceRepositoryTest : DatabaseTest() {
    private lateinit var repo: NamespaceRepository

    @BeforeTest
    fun setUpRepo() {
        repo = NamespaceRepository()
    }

    @Test
    fun testCreateAndGetById() = runTest {
        val ns = repo.create("test.ns")
        val found = repo.getById(ns.id)
        assertNotNull(found)
        assertEquals(ns.id, found.id)
        assertEquals("test.ns", found.name)
    }

    @Test
    fun testGetByName() = runTest {
        val ns = repo.create("unique.ns")
        val found = repo.getByName("unique.ns")
        assertNotNull(found)
        assertEquals(ns.id, found.id)
    }

    @Test
    fun testGetAll() = runTest {
        repo.create("a.ns")
        repo.create("b.ns")
        val all = repo.getAll()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.name == "a.ns" })
        assertTrue(all.any { it.name == "b.ns" })
    }

    @Test
    fun testGetByLibrary() = runTest {
        repo.create("lib1.core")
        repo.create("lib2.utils")
        val found = repo.getByLibrary("lib1")
        assertTrue(found.any { it.name == "lib1.core" })
        assertTrue(found.none { it.name == "lib2.utils" })
    }

    @Test
    fun testGetAllLibraryNames() = runTest {
        repo.create("libA.core")
        repo.create("libB.utils")
        repo.create("libA.extra")
        val names = repo.getAllLibraryNames()
        assertTrue(names.contains("libA"))
        assertTrue(names.contains("libB"))
        assertEquals(2, names.size)
    }

    @Test
    fun testGetOrCreate() = runTest {
        val ns1 = repo.getOrCreate("foo.bar")
        val ns2 = repo.getOrCreate("foo.bar")
        assertEquals(ns1.id, ns2.id)
        assertEquals(ns1.name, ns2.name)
    }
}
