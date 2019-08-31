package com.example.service

import com.example.dto.Account
import com.example.dto.MoneyTransferRequestDto
import com.example.dto.MoneyTransferResponseDto

interface AccountService {

    fun createAccount(account: Account): Account

    fun getAccount(accountNumber: String): Account

    fun transferMoney(moneyTransferDto: MoneyTransferRequestDto): MoneyTransferResponseDto

    fun getAllAccounts(): List<Account>

}