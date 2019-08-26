package com.example.service

import com.example.dto.Customer
import com.example.exceptions.CustomerNotFoundException
import com.example.exceptions.InvalidAgeException
import com.example.model.Customers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class CustomerService {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerService::class.java)
    }

    fun addCustomer(customer: Customer): Customer {
        log.info("Creating new customer with name- ${customer.name}, age - ${customer.age}, " +
                "city- ${customer.city}, phone - ${customer.phoneNumber}")

        if (customer.age <= 0) throw InvalidAgeException("Customer age must be greater than 0")

        var key = 0L
        transaction {
            key = (Customers.insert {
                it[name] = customer.name
                it[age] = customer.age
                it[city] = customer.city
                it[phoneNumber] = customer.phoneNumber
            } get Customers.id)
        }
        return getCustomer(key)
    }

    fun  getCustomer(customerId: Long): Customer {
        val cust = transaction {
            Customers.select {
                (Customers.id eq customerId)
            }.mapNotNull { toCustomer(it) }
                .singleOrNull()
        } ?: throw CustomerNotFoundException("Customer with Id: $customerId cannot be found")

        return cust
    }

    suspend fun getAllCustomers(): List<Customer> = transaction {
        Customers.selectAll().map { toCustomer(it) }
    }

    private fun toCustomer(row: ResultRow) =
        Customer(
            id = row[Customers.id],
            name = row[Customers.name],
            age = row[Customers.age],
            city = row[Customers.city],
            phoneNumber = row[Customers.phoneNumber]
        )

//    suspend fun  addCustomer(customer: Customer): Customer {
//        log.info("Creating new customer with name- ${customer.name}, age - ${customer.age}, " +
//                "city- ${customer.city}, phone - ${customer.phoneNumber}")
//        return transaction {
//            val newCustomer = Customer.new {
//                name = customer.name
//                age = customer.age
//                city = customer.city
//                phoneNumber = customer.phoneNumber
//            }
//
//            log.info("new customer created with ${newCustomer.id}")
//            return@transaction newCustomer
//        }
//    }
//
//    suspend fun getCustomer(customerId: Long): com.example.main.kotlin.dao.Customer {
//        log.info("Getting customer info for id: $customerId")
//        return transaction {
//            Customer.findById(customerId)
//                ?: throw CustomerNotFoundException("Customer with Id: $customerId cannot be found")
//        }
//    }
}