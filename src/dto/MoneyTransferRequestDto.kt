package com.example.dto

data class MoneyTransferRequestDto (
    var fromAccountNumber: String,
    var toAccountNumber: String,
    var amount: Double
)