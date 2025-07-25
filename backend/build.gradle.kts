plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.9.0" // ESSENCIAL!
    application
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Use Java 17, compatível com Kotlin 1.8.10
    }
}


group = "com.dynam"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.deployment.configuration=application.conf")
}



repositories {
    mavenCentral()
}

val ktorVersion = "2.3.7"
dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.3")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")  // Add SQLite dependency
    implementation("io.insert-koin:koin-ktor:4.0.4")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-core:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
    // Prevent parallel test execution to avoid SQLite database locking
    maxParallelForks = 1
    finalizedBy("cleanTestDb")
}

tasks.register("cleanTestDb") {
    group = "verification"
    description = "Deletes the test_shared.db and related files after all tests."
    doLast {
        listOf(
            "test_shared.db",
            "test_shared.db-journal",
            "test_shared.db-journal.backup",
            "test_shared.db.backup"
        ).forEach { fname ->
            val file = file(fname)
            if (file.exists()) file.delete()
        }
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

