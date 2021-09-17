package com.shubhamkumarwinner.data.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object UserTable: Table() {
    val userId: Column<Int> = integer("userId").autoIncrement()
    val name: Column<String> = varchar("name", 500)
    val email: Column<String> = varchar("email", 500).uniqueIndex()
    val password: Column<String> = varchar("password", 500)

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(userId)
}