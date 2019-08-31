package com.example.model

import org.jetbrains.exposed.sql.Table

object Accounts: Table() {
    var id = long("id").primaryKey().autoIncrement()
    val accountNumber = varchar("accountNumber", 64).uniqueIndex("ac_uk_acn")
    // Using double for simplicity. In a production environment, bigdecimal should be used.
    val balance = double("balance")
    val customerId = long("customerId").references(Customers.id)
}