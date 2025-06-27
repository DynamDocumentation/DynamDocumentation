// Arquivo: com/dynam/parsers/JsonParser.kt

package com.dynam.parsers

import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import com.dynam.models.*
import kotlinx.serialization.json.*
import java.io.File

class JsonParser {
    
    suspend fun parseJsonFile(filePath: String, namespaceName: String) {
        val jsonContent = File(filePath).readText()
        val jsonElement = Json.parseToJsonElement(jsonContent)
        val jsonObject = jsonElement.jsonObject
        
        // Criar ou obter namespace
        val namespace = Namespace.getAll().find { it.name == namespaceName } 
            ?: Namespace.create(namespaceName)
        
        // Descrição do módulo
        val moduleDescription = jsonObject["description"]?.jsonPrimitive?.content ?: ""
        
        // Processar classes
        jsonObject["classes"]?.jsonArray?.forEach { classElement ->
            processClass(classElement.jsonObject, namespace.id)
        }
        
        // Processar funções
        jsonObject["functions"]?.jsonArray?.forEach { functionElement ->
            processFunction(functionElement.jsonObject, namespace.id)
        }
        
        // Processar constantes
        jsonObject["constants"]?.jsonArray?.forEach { constantElement ->
            processConstant(constantElement.jsonObject, namespace.id)
        }
    }
    
    private suspend fun processClass(classObj: JsonObject, namespaceId: Int) {
        val className = classObj["name"]?.jsonPrimitive?.content ?: return
        val docstring = classObj["docstring"]?.jsonObject
        val description = docstring?.get("description")?.jsonPrimitive?.content ?: ""
        
        // Criar entidade da classe
        val classEntity = Entity.create(
            type = EntityType.CLASS,
            name = className,
            description = description,
            namespaceId = namespaceId
        )
        
        // Processar descrição como variável
        Variable.create(
            entityId = classEntity.id,
            type = VariableType.DESCRIPTION,
            name = "description",
            dataType = null,
            description = description,
            defaultValue = null
        )
        
        // Processar parâmetros da classe
        docstring?.get("parameters")?.jsonObject?.forEach { (paramName, paramObj) ->
            if (paramObj is JsonObject) {
                val paramType = paramObj["type"]?.jsonPrimitive?.content
                val paramDesc = paramObj["description"]?.jsonPrimitive?.content ?: ""
                
                Variable.create(
                    entityId = classEntity.id,
                    type = VariableType.PARAMETER,
                    name = paramName,
                    dataType = paramType,
                    description = paramDesc,
                    defaultValue = null
                )
            }
        }
        
        // Processar returns
        docstring?.get("returns")?.jsonPrimitive?.content?.let { returns ->
            Variable.create(
                entityId = classEntity.id,
                type = VariableType.RETURN,
                name = "return",
                dataType = null,
                description = returns,
                defaultValue = null
            )
        }
        
        // Processar raises
        docstring?.get("raises")?.jsonPrimitive?.content?.let { raises ->
            Variable.create(
                entityId = classEntity.id,
                type = VariableType.RAISE,
                name = "raises",
                dataType = null,
                description = raises,
                defaultValue = null
            )
        }
        
        // Processar examples
        docstring?.get("examples")?.jsonPrimitive?.content?.let { examples ->
            Variable.create(
                entityId = classEntity.id,
                type = VariableType.EXAMPLE,
                name = "examples",
                dataType = null,
                description = examples,
                defaultValue = null
            )
        }
        
        // Processar métodos (funções dentro da classe)
        classObj["methods"]?.jsonArray?.forEach { methodElement ->
            val methodObj = methodElement.jsonObject
            val methodName = methodObj["name"]?.jsonPrimitive?.content ?: return@forEach
            val signature = methodObj["signature"]?.jsonPrimitive?.content ?: ""
            val methodDocstring = methodObj["docstring"]?.jsonObject
            val methodDesc = methodDocstring?.get("description")?.jsonPrimitive?.content ?: ""
            
            // Criar entidade da função
            val functionEntity = Entity.create(
                type = EntityType.FUNCTION,
                name = "$className.$methodName",  // Prefixo com nome da classe para indicar método
                description = methodDesc,
                namespaceId = namespaceId
            )
            
            // Processar descrição
            Variable.create(
                entityId = functionEntity.id,
                type = VariableType.DESCRIPTION,
                name = "description",
                dataType = null,
                description = methodDesc,
                defaultValue = null
            )
            
            // Processar assinatura como um atributo especial
            Variable.create(
                entityId = functionEntity.id,
                type = VariableType.ATTRIBUTE,
                name = "signature",
                dataType = null,
                description = signature,
                defaultValue = null
            )
            
            // Processar parâmetros da função
            methodDocstring?.get("parameters")?.jsonObject?.forEach { (paramName, paramObj) ->
                if (paramObj is JsonObject) {
                    val paramType = paramObj["type"]?.jsonPrimitive?.content
                    val paramDesc = paramObj["description"]?.jsonPrimitive?.content ?: ""
                    
                    Variable.create(
                        entityId = functionEntity.id,
                        type = VariableType.PARAMETER,
                        name = paramName,
                        dataType = paramType,
                        description = paramDesc,
                        defaultValue = null
                    )
                }
            }
            
            // Processar returns
            methodDocstring?.get("returns")?.jsonPrimitive?.content?.let { returns ->
                Variable.create(
                    entityId = functionEntity.id,
                    type = VariableType.RETURN,
                    name = "return",
                    dataType = null,
                    description = returns,
                    defaultValue = null
                )
            }
            
            // Processar raises
            methodDocstring?.get("raises")?.jsonPrimitive?.content?.let { raises ->
                Variable.create(
                    entityId = functionEntity.id,
                    type = VariableType.RAISE,
                    name = "raises",
                    dataType = null,
                    description = raises,
                    defaultValue = null
                )
            }
            
            // Processar examples
            methodDocstring?.get("examples")?.jsonPrimitive?.content?.let { examples ->
                Variable.create(
                    entityId = functionEntity.id,
                    type = VariableType.EXAMPLE,
                    name = "examples",
                    dataType = null,
                    description = examples,
                    defaultValue = null
                )
            }
        }
    }
    
    private suspend fun processFunction(functionObj: JsonObject, namespaceId: Int) {
        val functionName = functionObj["name"]?.jsonPrimitive?.content ?: return
        val signature = functionObj["signature"]?.jsonPrimitive?.content ?: ""
        val docstring = functionObj["docstring"] ?: functionObj["documentation"]
        val docstringObj = docstring?.jsonObject
        val description = docstringObj?.get("description")?.jsonPrimitive?.content ?: ""
        
        // Criar entidade da função
        val functionEntity = Entity.create(
            type = EntityType.FUNCTION,
            name = functionName,
            description = description,
            namespaceId = namespaceId
        )
        
        // Processar descrição
        Variable.create(
            entityId = functionEntity.id,
            type = VariableType.DESCRIPTION,
            name = "description",
            dataType = null,
            description = description,
            defaultValue = null
        )
        
        // Processar assinatura como um atributo especial
        Variable.create(
            entityId = functionEntity.id,
            type = VariableType.ATTRIBUTE,
            name = "signature",
            dataType = null,
            description = signature,
            defaultValue = null
        )
        
        // Processar parâmetros
        docstringObj?.get("parameters")?.jsonObject?.forEach { (paramName, paramObj) ->
            if (paramObj is JsonObject) {
                val paramType = paramObj["type"]?.jsonPrimitive?.content
                val paramDesc = paramObj["description"]?.jsonPrimitive?.content ?: ""
                
                Variable.create(
                    entityId = functionEntity.id,
                    type = VariableType.PARAMETER,
                    name = paramName,
                    dataType = paramType,
                    description = paramDesc,
                    defaultValue = null
                )
            }
        }
        
        // Processar returns
        docstringObj?.get("returns")?.jsonPrimitive?.content?.let { returns ->
            Variable.create(
                entityId = functionEntity.id,
                type = VariableType.RETURN,
                name = "return",
                dataType = null,
                description = returns,
                defaultValue = null
            )
        }
        
        // Processar raises
        docstringObj?.get("raises")?.jsonPrimitive?.content?.let { raises ->
            Variable.create(
                entityId = functionEntity.id,
                type = VariableType.RAISE,
                name = "raises",
                dataType = null,
                description = raises,
                defaultValue = null
            )
        }
        
        // Processar examples
        docstringObj?.get("examples")?.jsonPrimitive?.content?.let { examples ->
            Variable.create(
                entityId = functionEntity.id,
                type = VariableType.EXAMPLE,
                name = "examples",
                dataType = null,
                description = examples,
                defaultValue = null
            )
        }
    }
    
    private suspend fun processConstant(constantObj: JsonObject, namespaceId: Int) {
        val constantName = constantObj["name"]?.jsonPrimitive?.content ?: return
        val value = constantObj["value"]?.toString() ?: ""
        
        // Criar uma entidade para o namespace (como um contêiner)
        val namespaceEntity = Entity.getEntitiesByNamespaceId(namespaceId, EntityType.CLASS)
            .find { it.name == namespaceName } 
            ?: Entity.create(
                type = EntityType.CLASS,
                name = namespaceName,
                description = "Namespace for constants",
                namespaceId = namespaceId
            )
        
        // Criar a constante
        Constant.create(
            entityId = namespaceEntity.id,
            name = constantName,
            value = value
        )
    }
}