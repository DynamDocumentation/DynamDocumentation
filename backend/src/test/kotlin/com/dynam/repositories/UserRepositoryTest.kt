package com.dynam.repositories

import com.dynam.DatabaseTest
import com.dynam.dtos.table.User
import com.dynam.database.tables.Users
import kotlin.test.*
import java.time.Instant

class UserRepositoryTest : DatabaseTest() {
    private lateinit var repo: UserRepository

    @BeforeTest
    fun setUpRepo() {
        repo = UserRepository()
    }

    @Test
    fun testCreateAndGetById() = runTest {
        val user = User(null, "alice", "alice@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond)
        val created = repo.create(user, "hash1")
        assertNotNull(created)
        val found = repo.getById(created.id!!)
        assertNotNull(found)
        assertEquals("alice", found.username)
    }

    @Test
    fun testGetByUsername() = runTest {
        val user = User(null, "bob", "bob@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond)
        val created = repo.create(user, "hash2")
        assertNotNull(created)
        val found = repo.getByUsername("bob")
        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @Test
    fun testGetByEmail() = runTest {
        val user = User(null, "carol", "carol@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond)
        val created = repo.create(user, "hash3")
        assertNotNull(created)
        val found = repo.getByEmail("carol@example.com")
        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @Test
    fun testGetAll() = runTest {
        repo.create(User(null, "dave", "dave@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond), "hash4")
        repo.create(User(null, "eve", "eve@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond), "hash5")
        val all = repo.getAll()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.username == "dave" })
        assertTrue(all.any { it.username == "eve" })
    }

    @Test
    fun testUpdateLastLogin() = runTest {
        val user = User(null, "frank", "frank@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond)
        val created = repo.create(user, "hash6")
        assertNotNull(created)
        val before = repo.getById(created.id!!)?.lastLogin ?: 0L
        Thread.sleep(1000) // ensure time changes
        val updated = repo.updateLastLogin(created.id!!)
        assertTrue(updated)
        val after = repo.getById(created.id!!)?.lastLogin ?: 0L
        assertTrue(after > before)
    }

    @Test
    fun testDelete() = runTest {
        val user = User(null, "grace", "grace@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond)
        val created = repo.create(user, "hash7")
        assertNotNull(created)
        val deleted = repo.delete(created.id!!)
        assertTrue(deleted)
        val found = repo.getById(created.id!!)
        assertNull(found)
    }

    @Test
    fun testVerifyCredentials() = runTest {
        val user = User(null, "heidi", "heidi@example.com", createdAt = Instant.now().epochSecond, lastLogin = Instant.now().epochSecond)
        val created = repo.create(user, "secretpass")
        assertNotNull(created)
        val valid = repo.verifyCredentials("heidi@example.com", "secretpass")
        assertNotNull(valid)
        assertEquals(created.id, valid.id)
        val invalid = repo.verifyCredentials("heidi@example.com", "wrongpass")
        assertNull(invalid)
    }
}
