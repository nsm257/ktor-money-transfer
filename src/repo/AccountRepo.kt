package com.example.repo

import com.example.dto.Account
import com.example.model.Accounts
import org.jetbrains.exposed.sql.*
import java.util.*

class AccountRepo {

    fun addAccount(account: Account) = Accounts.insert {
        it[accountNumber] = UUID.randomUUID().toString()
        it[customerId] = account.customerId
        it[balance] = account.amount
    } get Accounts.id

    fun getAccountById(id: Long) = Accounts.select {
        (Accounts.id eq id)
    }.mapNotNull { toAccount(it) }
        .singleOrNull()

    fun getAccountByAccountNumber(accountNumber: String) = Accounts.select {
        (Accounts.accountNumber eq accountNumber)
    }.mapNotNull { toAccount(it) }
        .singleOrNull()

    fun getAllAccounts(): List<Account> {
        return Accounts.selectAll().map { toAccount(it) }
    }

    fun updateAccountBalance(accountId: Long, amount: Double) =
        Accounts.update({ Accounts.id eq accountId }) {
            it[balance] = amount
        }

    private fun toAccount(row: ResultRow) = Account(
        id = row[Accounts.id],
        accountNumber = row[Accounts.accountNumber],
        amount = row[Accounts.balance],
        customerId = row[Accounts.customerId]
    )
}