package com.example.repo

import com.example.dto.Customer
import com.example.model.Customers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class CustomerRepo {

    fun addCustomer(customer: Customer) = Customers.insert {
        it[name] = customer.name
        it[age] = customer.age
        it[city] = customer.city
        it[phoneNumber] = customer.phoneNumber
    } get Customers.id

    fun getCustomer(id: Long): Customer? {
        return Customers.select {
            (Customers.id eq id)
        }.mapNotNull { toCustomer(it) }
            .singleOrNull()
    }

    fun getAllCustomers(): List<Customer> {
        return Customers.selectAll().map { toCustomer(it) }
    }

    private fun toCustomer(row: ResultRow) = Customer(
        id = row[Customers.id],
        name = row[Customers.name],
        age = row[Customers.age],
        city = row[Customers.city],
        phoneNumber = row[Customers.phoneNumber]
    )
}