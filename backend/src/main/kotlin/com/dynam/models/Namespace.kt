package com.dynam.models

import com.dynam.database.dbQuery
import com.dynam.database.tables.Namespaces
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

data class Namespace(
    val id: Int, 
    val name: String
) {
    companion object {
        fun fromRow(row: ResultRow) = Namespace(
            id = row[Namespaces.id],
            name = row[Namespaces.name]
        )
        
        suspend fun getAll(): List<Namespace> = dbQuery {
            Namespaces.selectAll().map { fromRow(it) }
        }
    }
}
