package com.dynam

import java.io.BufferedReader
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
    
    // Install the library using pip
    println("Installing library: $libraryName")
    val installProcess = ProcessBuilder("pip", "install", libraryName)
        .redirectErrorStream(true)
        .start()
    
    // Print output from the installation process
    val installReader = BufferedReader(InputStreamReader(installProcess.inputStream))
    var line: String?
    while (installReader.readLine().also { line = it } != null) {
        println(line)
    }
    
    val installExitCode = installProcess.waitFor()
    if (installExitCode != 0) {
        println("Error: Failed to install $libraryName (exit code: $installExitCode)")
        exitProcess(installExitCode)
    }
    
    // Generate documentation using the Python script
    println("Generating documentation for: $libraryName")
    val docProcess = ProcessBuilder("python3", "pop_general.py", libraryName)
        .redirectErrorStream(true)
        .start()
    
    // Print output from the documentation process
    val docReader = BufferedReader(InputStreamReader(docProcess.inputStream))
    while (docReader.readLine().also { line = it } != null) {
        println(line)
    }
    
    val docExitCode = docProcess.waitFor()
    if (docExitCode != 0) {
        println("Error: Failed to generate documentation for $libraryName (exit code: $docExitCode)")
        exitProcess(docExitCode)
    }
    
    println("Successfully installed and documented $libraryName")
}