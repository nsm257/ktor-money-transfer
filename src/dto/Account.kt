package com.example.dto

data class Account (
    var id : Long?,
    var customerId: Long,
    var amount: Double,
    var accountNumber: String?
)