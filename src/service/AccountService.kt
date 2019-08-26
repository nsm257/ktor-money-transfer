package com.example.service

import com.example.dto.Account
import com.example.dto.MoneyTransferRequestDto
import com.example.dto.MoneyTransferResponseDto
import com.example.exceptions.AccountNotFoundException
import com.example.exceptions.InsufficientFundsException
import com.example.exceptions.InvalidAmountException
import com.example.model.Accounts
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*

class AccountService {

    companion object {
        private val log = LoggerFactory.getLogger(AccountService::class.java)
    }

    private val RETRY_ATTEMPTS = 3
    // Todo fix foreign key exception
    suspend fun createAccount(account: Account): Account {
        log.info("Creating account for customer id: ${account.customerId} with starting amount as: ${account.amount}")

        if (account.amount < 0) throw InvalidAmountException("Account balance can not be less than 0")

        var key = 0L
        transaction {
            key = (Accounts.insert {
                // Todo: Change to seeded number
                it[accountNumber] = UUID.randomUUID().toString()
                it[customerId] = account.customerId
                it[balance] = account.amount
            } get Accounts.id)
        }
        return getAccountById(key)
    }

    suspend fun getAccountById(id: Long) = transaction {
        Accounts.select {
            (Accounts.id eq id)
        }.mapNotNull { toAccount(it) }
            .singleOrNull()
    } ?: throw AccountNotFoundException("Account with Id: $id cannot be found")

    fun getAccount(accountNumber: String) = transaction {
        Accounts.select {
            (Accounts.accountNumber eq accountNumber)
        }.mapNotNull { toAccount(it) }
            .singleOrNull()
    } ?: throw AccountNotFoundException("Account with account number : $accountNumber cannot be found")

    private fun toAccount(row: ResultRow) =
        Account(
            id = row[Accounts.id],
            accountNumber = row[Accounts.accountNumber],
            amount = row[Accounts.balance],
            customerId = row[Accounts.customerId]
        )

    private fun withdraw(accountNumber: String, amount: Double): Account {
        log.info("Withdrawing $amount from $accountNumber")
        val exAccount = getAccount(accountNumber)

        val bal = exAccount.amount
        if (bal < amount) throw InsufficientFundsException("Account with account number: $accountNumber does not have sufficient funds for withdrawl")

        exAccount.amount = bal - amount
        return exAccount
    }

    private fun deposit(accountNumber: String, amount: Double): Account {
        log.info("Depositing $amount in to $accountNumber")

        val exAccount = getAccount(accountNumber)
        exAccount.amount += amount

        return exAccount
    }

    suspend fun transferMoney(moneyTransferDto: MoneyTransferRequestDto): MoneyTransferResponseDto {
        log.info("Transferring ${moneyTransferDto.amount} from ${moneyTransferDto.fromAccountNumber} to ${moneyTransferDto.toAccountNumber}")

        if (moneyTransferDto.amount < 0) throw InvalidAmountException("Amount: ${moneyTransferDto.amount} has to be higher than 0")

        return transaction(Connection.TRANSACTION_SERIALIZABLE, RETRY_ATTEMPTS) {
            val toAccount = withdraw(moneyTransferDto.fromAccountNumber, moneyTransferDto.amount)
            val fromAccount = deposit(moneyTransferDto.toAccountNumber, moneyTransferDto.amount)
            return@transaction MoneyTransferResponseDto(fromAccount, toAccount)
        }
    }

    suspend fun getAllAccounts(): List<Account> = transaction {
        Accounts.selectAll().map { toAccount(it) }
    }

//    suspend fun createAccount(account: Account): Account =
//        transaction {
//            log.info("Creating account for customer id: ${account.customerId} with starting amount as: ${account.amount}")
//
//            val exCustomer = Customer.findById(account.customerId) ?: throw IllegalAccessException()
//
//            val newAccount = Account.new {
//                balance = account.amount
//                accountNumber = UUID.randomUUID().toString()
////                customer = exCustomer
//            }
//
//            return@transaction newAccount
//        }
//
//    fun getAccount(accountNumber: String): Account =
//        transaction {
//            log.info("Get Account info for account number $accountNumber")
//
//            val accounts = Account.find { Accounts.accountNumber eq accountNumber }
//            if (accounts.empty()) throw AccountNotFoundException("Account with account number: $accountNumber cannot be found")
//
//            return@transaction accounts.elementAt(0)
//        }
//
//    //Todo: Check if to be made private/synchronized
//    private fun  withdraw(accountNumber: String, amount: Double): Account {
//            log.info("Withdrawing $amount from $accountNumber")
//            val exAccount = getAccount(accountNumber)
//
//            val bal = exAccount.balance
//            if (bal < amount) throw InsufficientFundsException("Account with account number: $accountNumber does not have sufficient funds for withdrawl")
//
//            exAccount.balance = bal - amount
//            return exAccount
//    }
//
//    //Todo: Check if to be made private/synchronized
//    private fun deposit(accountNumber: String, amount: Double): Account {
//        log.info("Depositing $amount in to $accountNumber")
//
//        val exAccount = getAccount(accountNumber)
//        exAccount.balance += amount
//
//        return exAccount
//    }

//    suspend fun transferMoney(moneyTransferDto: MoneyTransferDto): Account {
//        log.info("Transferring ${moneyTransferDto.amount} from ${moneyTransferDto.fromAccountNumber} to ${moneyTransferDto.toAccountNumber}")
//
//        if (moneyTransferDto.amount < 0) throw InvalidAmountException("Amount: ${moneyTransferDto.amount} has to be higher than 0")
//
//        return transaction (Connection.TRANSACTION_SERIALIZABLE, RETRY_ATTEMPTS) {
//            val toAccount = withdraw(moneyTransferDto.fromAccountNumber, moneyTransferDto.amount)
//            val fromAccount = deposit(moneyTransferDto.toAccountNumber, moneyTransferDto.amount)
//            return@transaction toAccount
//        }
//    }
}