package com.example.dto

data class MoneyTransferResponseDto (
    val fromAccount: Account,
    val toAccount: Account
) {
    constructor(): this(Account(), Account())
}