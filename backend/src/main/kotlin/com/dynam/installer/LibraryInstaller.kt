package com.dynam

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess

/**
 * Library installer and documentation generator
 * Installs a Python library and generates its documentation
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: kotlin LibraryInstallerKt <library_name>")
        exitProcess(1)
    }

    val libraryName = args[0]
    
    try {
        installPythonLibrary(libraryName)
        generateLibraryDocumentation(libraryName)
        println("Successfully installed and documented $libraryName")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        exitProcess(1)
    }
}

/**
 * Installs a Python library using pip
 * 
 * @param libraryName The name of the library to install
 * @throws IOException If there's an issue with the process
 * @throws IllegalStateException If the installation fails
 */
fun installPythonLibrary(libraryName: String) {
    println("Installing library: $libraryName")
    val installProcess = ProcessBuilder("pip", "install", libraryName)
        .redirectErrorStream(true)
        .start()
    
    // Print output from the installation process
    runProcess(installProcess).also { exitCode ->
        if (exitCode != 0) {
            throw IllegalStateException("Failed to install $libraryName (exit code: $exitCode)")
        }
    }
}

/**
 * Generates documentation for a Python library using pop_general.py
 * 
 * @param libraryName The name of the library to document
 * @throws IOException If there's an issue with the process
 * @throws IllegalStateException If documentation generation fails
 */
fun generateLibraryDocumentation(libraryName: String) {
    println("Generating documentation for: $libraryName")
    val docProcess = ProcessBuilder("python3", "pop_general.py", libraryName)
        .redirectErrorStream(true)
        .start()
    
    // Print output from the documentation process
    runProcess(docProcess).also { exitCode ->
        if (exitCode != 0) {
            throw IllegalStateException("Failed to generate documentation for $libraryName (exit code: $exitCode)")
        }
    }
}

/**
 * Runs a process and streams its output to stdout
 * 
 * @param process The process to run
 * @return The exit code of the process
 */
fun runProcess(process: Process): Int {
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        println(line)
    }
    
    return process.waitFor()
}