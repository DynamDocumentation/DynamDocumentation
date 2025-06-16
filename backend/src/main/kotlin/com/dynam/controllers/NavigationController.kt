// package com.dynam.controllers
// import com.dynam.models.*
// import kotlinx.coroutines.runBlocking

// class NavigationController {
//     private val namespaceModel = NamespaceModel()
//     private val functionModel = FunctionModel()
//     private val classModel = ClassModel()

//     // Currently works only with one library. When more are needed,
//     // this method will need an argument "libName" or smt
//     // The return type must be revised later...
//     fun getAllPathsForNavigation() : Map<String, Map<String, List<String>>> {
//         val namespaces = runBlocking { namespaceModel.getAllNamespaces() }
//         return namespaces.associate { ns ->
//             val functions = functionModel.getAllEntityNamesFrom(ns)
//             val classes = classModel.getAllEntityNamesFrom(ns)
//             ns.name to mapOf("functions" to functions, "classes" to classes)
//         }
//     }
// }