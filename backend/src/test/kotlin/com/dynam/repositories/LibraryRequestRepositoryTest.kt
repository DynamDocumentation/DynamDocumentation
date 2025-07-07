package com.dynam.repositories

import com.dynam.DatabaseTest
import kotlin.test.*

class LibraryRequestRepositoryTest : DatabaseTest() {
    private lateinit var repo: LibraryRequestRepository

    @BeforeTest
    fun setUpRepo() {
        repo = LibraryRequestRepository()
    }

    @Test
    fun testCreateAndGetById() = runTest {
        val created = repo.create("numpy")
        assertNotNull(created)
        assertEquals("numpy", created.name)
        assertFalse(created.accepted)
        val found = repo.getById(created.id)
        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @Test
    fun testGetByName() = runTest {
        val created = repo.create("scipy")
        val found = repo.getByName("scipy")
        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @Test
    fun testGetAll() = runTest {
        repo.create("pandas")
        repo.create("matplotlib")
        val all = repo.getAll()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.name == "pandas" })
        assertTrue(all.any { it.name == "matplotlib" })
    }

    @Test
    fun testGetByAcceptedStatus() = runTest {
        val req1 = repo.create("seaborn")
        val req2 = repo.create("plotly")
        repo.updateAcceptanceStatus(req1.id, true)
        val accepted = repo.getByAcceptedStatus(true)
        val notAccepted = repo.getByAcceptedStatus(false)
        assertTrue(accepted.any { it.id == req1.id })
        assertTrue(notAccepted.any { it.id == req2.id })
    }

    @Test
    fun testUpdateAcceptanceStatus() = runTest {
        val created = repo.create("sympy")
        val updated = repo.updateAcceptanceStatus(created.id, true)
        assertTrue(updated)
        val found = repo.getById(created.id)
        assertNotNull(found)
        assertTrue(found.accepted)
    }

    @Test
    fun testDelete() = runTest {
        val created = repo.create("networkx")
        val deleted = repo.delete(created.id)
        assertTrue(deleted)
        val found = repo.getById(created.id)
        assertNull(found)
    }
}
