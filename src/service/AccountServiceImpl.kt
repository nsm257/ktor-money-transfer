package com.example.service

import com.example.dto.Account
import com.example.dto.MoneyTransferRequestDto
import com.example.dto.MoneyTransferResponseDto
import com.example.exceptions.AccountNotFoundException
import com.example.exceptions.CustomerNotFoundException
import com.example.exceptions.InsufficientFundsException
import com.example.exceptions.InvalidAmountException
import com.example.model.Accounts.id
import com.example.repo.AccountRepo
import com.example.repo.CustomerRepo
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.concurrent.TimeUnit
import java.util.Random
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


class AccountServiceImpl: AccountService {

    companion object {
        private val log = LoggerFactory.getLogger(AccountService::class.java)
    }

    private val RETRY_ATTEMPTS = 3
    private val FIXED_DELAY = 1
    private val RANDOM_DELAY = 2
    private val TIMEOUT = TimeUnit.SECONDS.toNanos(2)

    private val accountRepo = AccountRepo()
    private val customerRepo = CustomerRepo()

    var _lock: Lock = ReentrantLock()

    override fun createAccount(account: Account): Account {
        log.info("Creating account for customer id: ${account.customerId} with starting amount as: ${account.amount}")

        if (account.amount < 0) throw InvalidAmountException("Account balance can not be less than 0")

        transaction {
            if (customerRepo.getCustomer(account.customerId) == null) throw CustomerNotFoundException("Incorrect customer Id: ${account.customerId} provided")
        }

        return transaction {
            val key = accountRepo.addAccount(account)
            return@transaction accountRepo.getAccountById(key)
        } ?: throw AccountNotFoundException("Account with Id: $id cannot be found")
    }

    override fun getAccount(accountNumber: String) = transaction {
        accountRepo.getAccountByAccountNumber(accountNumber)
    } ?: throw AccountNotFoundException("Account with account number : $accountNumber cannot be found")


    fun _transferMoney(moneyTransferDto: MoneyTransferRequestDto): Pair<Boolean, MoneyTransferResponseDto> {

        val fromAccount = accountRepo.getAccountByAccountNumber(moneyTransferDto.fromAccountNumber)
            ?: throw AccountNotFoundException("Account with account number : ${moneyTransferDto.fromAccountNumber} cannot be found")

        if (fromAccount.amount < moneyTransferDto.amount) {
            throw InsufficientFundsException("Account with account number: ${moneyTransferDto.fromAccountNumber} does not have sufficient funds for withdrawal")
        }

        val newFromBalance = fromAccount.amount - moneyTransferDto.amount
        accountRepo.updateAccountBalance(fromAccount.id!!, newFromBalance)

        val toAccount = accountRepo.getAccountByAccountNumber(moneyTransferDto.toAccountNumber)
            ?: throw AccountNotFoundException("Account with account number : ${moneyTransferDto.toAccountNumber} cannot be found")

        val newToBalance = toAccount.amount + moneyTransferDto.amount
        accountRepo.updateAccountBalance(toAccount.id!!, newToBalance)

        val newFromAccount = accountRepo.getAccountByAccountNumber(moneyTransferDto.fromAccountNumber)
        val newToAccount = accountRepo.getAccountByAccountNumber(moneyTransferDto.toAccountNumber)

        log.info("balance of ${newFromAccount?.id} changed from ${fromAccount.amount} to ${newFromAccount?.amount}")
        log.info("balance of ${newToAccount?.id} changed from ${toAccount.amount} to ${newToAccount?.amount}")

        val res = MoneyTransferResponseDto(newFromAccount!!, newToAccount!!)

        return Pair(true, res)
    }

    override fun transferMoney(moneyTransferDto: MoneyTransferRequestDto): MoneyTransferResponseDto {
        log.info("Transferring ${moneyTransferDto.amount} from ${moneyTransferDto.fromAccountNumber} to ${moneyTransferDto.toAccountNumber}")

        if (moneyTransferDto.amount <= 0) throw InvalidAmountException("Transfer Amount: ${moneyTransferDto.amount} has to be higher than 0")

        val transactionTimeLimit = System.nanoTime() + TIMEOUT
        var res: MoneyTransferResponseDto? = null
        var done = false

        while (System.nanoTime() < transactionTimeLimit && !done) {

            if (_lock.tryLock()) {
                try {
                    log.info("******** Received the lock ***********")
                    transaction(Connection.TRANSACTION_SERIALIZABLE, RETRY_ATTEMPTS) {
                        val (done1, res1) = _transferMoney(moneyTransferDto)
                        done = done1
                        res = res1
                    }

                } finally {
                    log.info("********** Released the lock ***********")
                    _lock.unlock()
                }
            }

            try {
                TimeUnit.NANOSECONDS.sleep(FIXED_DELAY + Random().nextLong() % RANDOM_DELAY)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw RuntimeException(e)
            }
        }

        return res!!
    }

    override fun getAllAccounts(): List<Account> = transaction {
        accountRepo.getAllAccounts()
    }
}