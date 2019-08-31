package com.example.service

import com.example.dto.Customer
import com.example.exceptions.CustomerNotFoundException
import com.example.exceptions.InvalidAgeException
import com.example.repo.CustomerRepo
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class CustomerServiceImpl : CustomerService {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerServiceImpl::class.java)
    }

    private val customerRepo = CustomerRepo()

    override fun addCustomer(customer: Customer): Customer {
        log.info("Creating new customer with name- ${customer.name}, age - ${customer.age}, " +
                "city- ${customer.city}, phone - ${customer.phoneNumber}")

        if (customer.age <= 0) throw InvalidAgeException("Customer age must be greater than 0")

        var key = 0L
        return transaction {
            key = customerRepo.addCustomer(customer)
            return@transaction customerRepo.getCustomer(key)
        } ?: throw CustomerNotFoundException("Customer with Id: $key cannot be found")
    }

    override fun  getCustomer(customerId: Long): Customer {
        log.info("Getting customer with id - $customerId")

        return transaction {
            return@transaction customerRepo.getCustomer(customerId)
        } ?: throw CustomerNotFoundException("Customer with Id: $customerId cannot be found")
    }

    override fun getAllCustomers(): List<Customer> = transaction {
        log.info("Getting all customers")

        return@transaction customerRepo.getAllCustomers()
    }
}