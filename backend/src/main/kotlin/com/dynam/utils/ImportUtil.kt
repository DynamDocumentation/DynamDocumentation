// // Arquivo: com/dynam/utils/ImportUtil.kt

// package com.dynam.utils

// import com.dynam.parsers.JsonParser
// import java.io.File

// class ImportUtil {
    
//     suspend fun importJsonDirectory(directoryPath: String) {
//         val directory = File(directoryPath)
//         val parser = JsonParser()
        
//         if (directory.isDirectory) {
//             directory.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
//                 val namespaceName = file.nameWithoutExtension
//                 println("Importing file: ${file.name} as namespace: $namespaceName")
                
//                 try {
//                     parser.parseJsonFile(file.absolutePath, namespaceName)
//                     println("Successfully imported ${file.name}")
//                 } catch (e: Exception) {
//                     println("Error importing ${file.name}: ${e.message}")
//                     e.printStackTrace()
//                 }
//             }
//         } else {
//             println("$directoryPath is not a directory")
//         }
//     }
// }