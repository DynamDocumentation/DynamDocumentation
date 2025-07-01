// Arquivo: com/dynam/utils/ImportUtil.kt

package com.dynam.utils

import kotlinx.coroutines.delay
import org.jetbrains.exposed.exceptions.ExposedSQLException
import com.dynam.database.dbQuery
import com.dynam.database.tables.ProcessedFiles
import com.dynam.parsers.JsonParser
import java.io.File
import java.security.MessageDigest
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.slf4j.LoggerFactory
import kotlinx.coroutines.delay

class ImportUtil {
    private val logger = LoggerFactory.getLogger(ImportUtil::class.java)
    private val parser = JsonParser()
    
    suspend fun importJsonDirectory(directoryPath: String) {
        val directory = File(directoryPath)
        
        if (directory.isDirectory) {
            directory.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
                importSingleFile(file)
            }
        } else {
            logger.warn("$directoryPath não é um diretório")
        }
    }
    
    suspend fun importSingleFile(file: File) {
        if (file.extension != "json") {
            logger.warn("Arquivo ${file.name} não é um arquivo JSON. Ignorando.")
            return
        }
        
        val namespaceName = file.nameWithoutExtension
        logger.info("Verificando arquivo: ${file.absolutePath}")
        
        // Calcular hash do conteúdo do arquivo
        val fileHash = calculateFileHash(file)
        
        // Verificar se o arquivo já foi processado com o mesmo hash
        val alreadyProcessed = dbQuery {
            ProcessedFiles.select { 
                ProcessedFiles.filePath eq file.absolutePath 
            }.firstOrNull()?.let { row ->
                row[ProcessedFiles.hash] == fileHash
            } ?: false
        }
        
        if (alreadyProcessed) {
            logger.info("Arquivo ${file.name} já foi processado anteriormente e não foi modificado. Ignorando.")
            return
        }
        
        // Se chegou aqui, o arquivo é novo ou foi modificado
        logger.info("Importando arquivo: ${file.name} como namespace: $namespaceName")
        
        try {
            parser.parseJsonFile(file.absolutePath, namespaceName)
            
            // Registrar o arquivo como processado
            dbQuery {
                ProcessedFiles.insert {
                    it[filePath] = file.absolutePath
                    it[hash] = fileHash
                }
            }
            
            logger.info("Arquivo ${file.name} importado com sucesso")
        } catch (e: Exception) {
            logger.error("Erro ao importar ${file.name}: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun calculateFileHash(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    suspend fun importSingleFile(filePath: String): Boolean {
        // Lógica para verificar se o arquivo já foi processado
        val alreadyProcessed = try {
            // Adiciona retentativas para operações de banco de dados
            var retries = 0
            val maxRetries = 3
            
            while (retries < maxRetries) {
                try {
                    dbQuery {
                        // Consulta existente
                        ProcessedFiles.select { ProcessedFiles.filePath eq filePath }
                            .firstOrNull()
                            ?.let { /* código existente */ }
                    }
                    break // Sai do loop se for bem-sucedido
                } catch (e: ExposedSQLException) {
                    if (e.cause?.message?.contains("database is locked") == true) {
                        retries++
                        if (retries < maxRetries) {
                            // Espera exponencial antes de tentar novamente
                            delay(100L * (1 shl retries))
                            continue
                        }
                    }
                    throw e // Relança se não for erro de bloqueio ou após retentativas
                }
            }
            // Resto do código...
        } catch (e: Exception) {
            logger.error("Error checking if file was processed: ${e.message}", e)
            false
        }
        
        // Resto do método...
        return true // ou o valor apropriado
    }
}