package com.example.dto

data class Account (
    var id : Long?,
    var customerId: Long,
    var amount: Double,
    var accountNumber: String?
) {
    constructor() : this(0, 121, 0.0, "abc")
}