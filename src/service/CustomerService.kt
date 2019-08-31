package com.example.service

import com.example.dto.Customer

interface CustomerService {

    fun addCustomer(customer: Customer): Customer

    fun getCustomer(customerId: Long): Customer

    fun getAllCustomers(): List<Customer>
}