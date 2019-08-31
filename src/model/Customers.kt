package com.example.model

import org.jetbrains.exposed.sql.Table

object Customers : Table() {
    var id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val age = integer("age")
    val city = varchar("city", length = 50)
    var phoneNumber = varchar("phoneNumber", length = 15)
}