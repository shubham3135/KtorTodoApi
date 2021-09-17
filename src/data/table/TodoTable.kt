package com.shubhamkumarwinner.data.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object TodoTable: Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val userId: Column<Int> = integer("userId").references(UserTable.userId)
    val todo: Column<String> = varchar("todo", 500)
    val done: Column<Boolean> = bool("done")

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(id)
}