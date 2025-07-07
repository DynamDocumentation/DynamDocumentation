// package com.dynam

// import com.dynam.utils.ImportUtil
// import kotlinx.coroutines.runBlocking
// import org.slf4j.LoggerFactory
// import java.io.BufferedReader
// import java.io.File
// import java.io.InputStreamReader
// import java.nio.file.Paths
// import kotlin.io.path.exists

// /**
//  * Library installer and documentation generator
//  * Installs a Python library and generates its documentation
//  */
// class LibraryInstaller {
//     private val logger = LoggerFactory.getLogger(LibraryInstaller::class.java)
//     private val importUtil = ImportUtil()
//     private val pythonScriptPath = "python/pop_general.py"
//     private val outputDir = "output"
    
//     fun installLibrary(libraryName: String): Boolean {
//         logger.info("Iniciando instalação da biblioteca: $libraryName")
        
//         try {
//             // 1. Executar o script Python para gerar documentação
//             val success = executeDocumentationScript(libraryName)
//             if (!success) {
//                 logger.error("Falha ao gerar documentação para $libraryName")
//                 return false
//             }
            
//             // 2. Importar os arquivos JSON gerados
//             val libraryOutputDir = File(outputDir, libraryName)
//             if (!libraryOutputDir.exists() || !libraryOutputDir.isDirectory) {
//                 logger.error("Diretório de saída não encontrado para biblioteca $libraryName")
//                 return false
//             }
            
//             // 3. Importar a documentação para o banco de dados
//             runBlocking {
//                 importUtil.importJsonDirectory(libraryOutputDir.absolutePath)
//             }
            
//             logger.info("Biblioteca $libraryName instalada com sucesso")
//             return true
//         } catch (e: Exception) {
//             logger.error("Erro ao instalar biblioteca $libraryName: ${e.message}")
//             e.printStackTrace()
//             return false
//         }
//     }
    
//     private fun executeDocumentationScript(libraryName: String): Boolean {
//         val scriptFile = File(pythonScriptPath)
//         if (!scriptFile.exists()) {
//             logger.error("Script Python não encontrado: $pythonScriptPath")
//             return false
//         }
        
//         val process = ProcessBuilder("python", scriptFile.absolutePath, libraryName)
//             .redirectErrorStream(true)
//             .start()
        
//         val reader = BufferedReader(InputStreamReader(process.inputStream))
//         var line: String?
        
//         while (reader.readLine().also { line = it } != null) {
//             logger.info("[Python] $line")
//         }
        
//         val exitCode = process.waitFor()
//         return exitCode == 0
//     }
// }