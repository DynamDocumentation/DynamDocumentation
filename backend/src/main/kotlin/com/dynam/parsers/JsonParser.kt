// Arquivo: com/dynam/parsers/JsonParser.kt

package com.dynam.parsers

import com.dynam.enums.EntityType
import com.dynam.enums.VariableType
import com.dynam.models.*
import kotlinx.serialization.json.*
import java.io.File
import org.slf4j.LoggerFactory

class JsonParser {
    private val logger = LoggerFactory.getLogger(JsonParser::class.java)
    
    suspend fun parseJsonFile(filePath: String, namespaceName: String) {
        val jsonContent = File(filePath).readText()
        val jsonElement = Json.parseToJsonElement(jsonContent)
        val jsonObject = jsonElement.jsonObject
        
        // Create or get namespace
        val namespace = Namespace.create(namespaceName)
        
        // Module description
        val moduleDescription = jsonObject["description"]?.jsonPrimitive?.content ?: ""
        
        // Process classes
        jsonObject["classes"]?.jsonArray?.forEach { classElement ->
            processClass(classElement.jsonObject, namespace.id)
        }
        
        // Process functions
        jsonObject["functions"]?.jsonArray?.forEach { functionElement ->
            processFunction(functionElement.jsonObject, namespace.id)
        }
        
        // Process constants
        jsonObject["constants"]?.jsonArray?.forEach { constantElement ->
            processConstant(constantElement.jsonObject, namespace.id)
        }
    }
    
    private suspend fun processClass(classObj: JsonObject, namespaceId: Int) {
        val className = classObj["name"]?.jsonPrimitive?.content ?: return
        val docstring = classObj["docstring"]?.jsonObject
        val description = docstring?.get("description")?.jsonPrimitive?.content ?: ""
        
        // Create class entity
        val classEntity = Entity.create(
            type = EntityType.CLASS,
            name = className,
            description = description,
            namespaceId = namespaceId
        )
        
        // Process description as variable
        Variable.create(
            classId = classEntity.id,
            functionId = null,
            type = VariableType.DESCRIPTION,
            name = "description",
            dataType = null,
            description = description,
            defaultValue = null
        )
        
        // Process class parameters
        docstring?.get("parameters")?.jsonObject?.let { parameters ->
            processClassParameters(classEntity.id, parameters)
        }
        
        // Process returns
        docstring?.get("returns")?.jsonPrimitive?.content?.let { returns ->
            Variable.create(
                classId = classEntity.id,
                functionId = null,
                type = VariableType.RETURN,
                name = "return",
                dataType = null,
                description = returns,
                defaultValue = null
            )
        }
        
        // Process raises
        docstring?.get("raises")?.jsonPrimitive?.content?.let { raises ->
            Variable.create(
                classId = classEntity.id,
                functionId = null,
                type = VariableType.RAISE,
                name = "raises",
                dataType = null,
                description = raises,
                defaultValue = null
            )
        }
        
        // Process examples
        docstring?.get("examples")?.jsonPrimitive?.content?.let { examples ->
            Variable.create(
                classId = classEntity.id,
                functionId = null,
                type = VariableType.EXAMPLE,
                name = "examples",
                dataType = null,
                description = examples,
                defaultValue = null
            )
        }
        
        // Process methods (functions inside the class)
        classObj["methods"]?.jsonArray?.forEach { methodElement ->
            val methodObj = methodElement.jsonObject
            val methodName = methodObj["name"]?.jsonPrimitive?.content ?: return@forEach
            val signature = methodObj["signature"]?.jsonPrimitive?.content ?: ""
            val methodDocstring = methodObj["docstring"]?.jsonObject
            val methodDesc = methodDocstring?.get("description")?.jsonPrimitive?.content ?: ""
            
            // Create method entity
            val functionEntity = Entity.create(
                type = EntityType.FUNCTION,
                name = "$className.$methodName",  // Prefix with class name to indicate method
                description = methodDesc,
                namespaceId = namespaceId
            )
            
            // Process description
            Variable.create(
                classId = null,
                functionId = functionEntity.id,
                type = VariableType.DESCRIPTION,
                name = "description",
                dataType = null,
                description = methodDesc,
                defaultValue = null
            )
            
            // Process signature as a special attribute
            Variable.create(
                classId = null,
                functionId = functionEntity.id,
                type = VariableType.ATTRIBUTE,
                name = "signature",
                dataType = null,
                description = signature,
                defaultValue = null
            )
            
            // Process function parameters
            methodDocstring?.get("parameters")?.jsonObject?.let { parameters ->
                processFunctionParameters(functionEntity.id, parameters)
            }
            
            // Process returns
            methodDocstring?.get("returns")?.jsonPrimitive?.content?.let { returns ->
                Variable.create(
                    classId = null,
                    functionId = functionEntity.id,
                    type = VariableType.RETURN,
                    name = "return",
                    dataType = null,
                    description = returns,
                    defaultValue = null
                )
            }
            
            // Process raises
            methodDocstring?.get("raises")?.jsonPrimitive?.content?.let { raises ->
                Variable.create(
                    classId = null,
                    functionId = functionEntity.id,
                    type = VariableType.RAISE,
                    name = "raises",
                    dataType = null,
                    description = raises,
                    defaultValue = null
                )
            }
            
            // Process examples
            methodDocstring?.get("examples")?.jsonPrimitive?.content?.let { examples ->
                Variable.create(
                    classId = null,
                    functionId = functionEntity.id,
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
        
        // Create function entity
        val functionEntity = Entity.create(
            type = EntityType.FUNCTION,
            name = functionName,
            description = description,
            namespaceId = namespaceId
        )
        
        // Process description
        Variable.create(
            classId = null,
            functionId = functionEntity.id,
            type = VariableType.DESCRIPTION,
            name = "description",
            dataType = null,
            description = description,
            defaultValue = null
        )
        
        // Process signature as a special attribute
        Variable.create(
            classId = null,
            functionId = functionEntity.id,
            type = VariableType.ATTRIBUTE,
            name = "signature",
            dataType = null,
            description = signature,
            defaultValue = null
        )
        
        // Process parameters
        docstringObj?.get("parameters")?.jsonObject?.let { parameters ->
            processFunctionParameters(functionEntity.id, parameters)
        }
        
        // Process returns
        docstringObj?.get("returns")?.jsonPrimitive?.content?.let { returns ->
            Variable.create(
                classId = null,
                functionId = functionEntity.id,
                type = VariableType.RETURN,
                name = "return",
                dataType = null,
                description = returns,
                defaultValue = null
            )
        }
        
        // Process raises
        docstringObj?.get("raises")?.jsonPrimitive?.content?.let { raises ->
            Variable.create(
                classId = null,
                functionId = functionEntity.id,
                type = VariableType.RAISE,
                name = "raises",
                dataType = null,
                description = raises,
                defaultValue = null
            )
        }
        
        // Process examples
        docstringObj?.get("examples")?.jsonPrimitive?.content?.let { examples ->
            Variable.create(
                classId = null,
                functionId = functionEntity.id,
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
        
        // Create an entity for the namespace (as a container)
        val currentNamespace = Namespace.getAll().find { it.id == namespaceId }
        val namespaceName = currentNamespace?.name ?: "unknown"
        
        val namespaceEntity = Entity.getEntitiesByNamespaceId(namespaceId, EntityType.CLASS)
            .find { it.name == namespaceName } ?: return
        
        // Create the constant
        Constant.create(
            entityId = namespaceEntity.id,
            name = constantName,
            value = value
        )
    }
    
    // Update your methods to pass the right ID type
    private suspend fun processClassParameters(classId: Int, parameters: JsonObject) {
        for ((paramName, paramInfo) in parameters.entries) {
            // Create parameter with classId
            Variable.create(
                classId = classId,
                functionId = null,
                type = VariableType.PARAMETER,
                name = paramName,
                dataType = null,
                description = paramInfo.jsonObject["description"]?.jsonPrimitive?.content ?: "",
                defaultValue = null
            )
        }
    }
    
    private suspend fun processFunctionParameters(functionId: Int, parameters: JsonObject) {
        for ((paramName, paramInfo) in parameters.entries) {
            // Create parameter with functionId
            Variable.create(
                classId = null,
                functionId = functionId,
                type = VariableType.PARAMETER,
                name = paramName,
                dataType = null,
                description = paramInfo.jsonObject["description"]?.jsonPrimitive?.content ?: "",
                defaultValue = null
            )
        }
    }
}