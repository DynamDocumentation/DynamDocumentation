package com.dynam

import java.util.UUID
import kotlin.test.BeforeTest

/**
 * Base class for integration tests that require only a JDBC URL for Ktor config.
 * Does NOT connect or create tables. Use with Ktor test engine.
 */
abstract class IntegrationTestSetup {
    protected lateinit var jdbcUrl: String

    @BeforeTest
    fun setupJdbcUrl() {
        val dbFile = "test_shared.db"
        jdbcUrl = "jdbc:sqlite:$dbFile"
    }
}
