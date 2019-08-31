package service

import com.example.dto.Account
import com.example.dto.Customer
import com.example.dto.MoneyTransferRequestDto
import com.example.exceptions.*
import com.example.service.AccountServiceImpl
import com.example.service.CustomerServiceImpl
import common.ServerTest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AccountServiceImplTest : ServerTest() {

    private val accountService = AccountServiceImpl()
    private val customerService = CustomerServiceImpl()

    @Test
    fun testAddAccount() = runBlocking {
        // given
        val customer = Customer(0, "John Doe", 25, "London", "1234567890")

        // when
        val savedCustomer = addCustomer(customer)
        val account = Account(0, savedCustomer.id!!, 30.0, null)
        val newAccount = addAccount(account)

        // then
        val retrieved = accountService.getAccount(newAccount.accountNumber!!)
        assertThat(retrieved).isEqualTo(newAccount)
        assertThat(retrieved.customerId).isEqualTo(account.customerId)
        assertThat(retrieved.amount).isEqualTo(account.amount)
        assertThat(retrieved.accountNumber).isEqualTo(newAccount.accountNumber)

        Unit
    }

    @Test
    fun testGetAllAccounts() = runBlocking {
        // given
        val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
        val cust2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
        val savedCustomer1 = addCustomer(cust1)
        val savedCustomer2 = addCustomer(cust2)

        // when
        val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
        val account2  = Account(0, savedCustomer2.id!!, 300.0, null)
        val savedAccount1 = addAccount(account1)
        val savedAccount2 = addAccount(account2)

        val accounts = accountService.getAllAccounts()

        // then
        assertThat(accounts).hasSize(2)
        assertThat(accounts).extracting("customerId").containsExactlyInAnyOrder(account1.customerId, account2.customerId)
        assertThat(accounts).extracting("amount").containsExactlyInAnyOrder(account1.amount, account2.amount)
        assertThat(accounts).extracting("accountNumber").containsExactlyInAnyOrder(savedAccount1.accountNumber, savedAccount2.accountNumber)

        Unit
    }

    @Test
    fun testTransferMoney() = runBlocking {
        // given
        val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
        val cust2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
        val savedCustomer1 = addCustomer(cust1)
        val savedCustomer2 = addCustomer(cust2)

        val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
        val account2  = Account(0, savedCustomer2.id!!, 300.0, null)
        val savedAccount1 = addAccount(account1)
        val savedAccount2 = addAccount(account2)

        val transferAmount = 100.0

        // when
        val res = accountService.transferMoney(MoneyTransferRequestDto(savedAccount1.accountNumber!!, savedAccount2.accountNumber!!, transferAmount))

        // then
        assertThat(res.fromAccount.id).isEqualTo(savedAccount1.id)
        assertThat(res.fromAccount.accountNumber).isEqualTo(savedAccount1.accountNumber)
        assertThat(res.fromAccount.amount).isEqualTo(savedAccount1.amount - transferAmount)
        assertThat(res.fromAccount.customerId).isEqualTo(savedAccount1.customerId)

        assertThat(res.toAccount.id).isEqualTo(savedAccount2.id)
        assertThat(res.toAccount.accountNumber).isEqualTo(savedAccount2.accountNumber)
        assertThat(res.toAccount.amount).isEqualTo(savedAccount2.amount + transferAmount)
        assertThat(res.toAccount.customerId).isEqualTo(savedAccount2.customerId)

        Unit
    }

    @Nested
    inner class ErrorCases {

        @Test
        fun testCreateAccountWithInvalidCustomer() = runBlocking {

            val customerId = 500L
            val exception = assertThrows(CustomerNotFoundException::class.java) {
                accountService.createAccount(Account(0, customerId, 25.0, null))
            }

            assertEquals("Incorrect customer Id: $customerId provided", exception.message)
            Unit
        }

        @Test
        fun testCreateAccountWithInvalidAmount() = runBlocking {

            val exception = assertThrows(InvalidAmountException::class.java) {
                val customer = Customer(0, "John Doe", 25, "London", "1234567890")
                val savedCustomer = addCustomer(customer)
                accountService.createAccount(Account(0, savedCustomer.id!!, -25.0, null))
            }

            assertEquals("Account balance can not be less than 0", exception.message)
            Unit
        }


        @Test
        fun testGetAccountWithInvalidAccountNumber() = runBlocking {

            val accNo = "abc"
            val exception = assertThrows(AccountNotFoundException::class.java) {
                accountService.getAccount(accNo)
            }

            assertEquals("Account with account number : $accNo cannot be found", exception.message)
            Unit
        }

        @Test
        fun testTransferMoneyWithInvalidAccount() = runBlocking {

            val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
            val savedCustomer1 = addCustomer(cust1)

            val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
            val savedAccount1 = addAccount(account1)

            val transferAmount = 50.0

            val fromAccountNumber = "abc"
            val exception1 = assertThrows(AccountNotFoundException::class.java) {
                accountService.transferMoney(MoneyTransferRequestDto(fromAccountNumber, savedAccount1.accountNumber!!, transferAmount))
            }

            assertEquals("Account with account number : $fromAccountNumber cannot be found", exception1.message)

            val toAccountNumber = "abc"
            val exception2 = assertThrows(AccountNotFoundException::class.java) {
                accountService.transferMoney(MoneyTransferRequestDto(savedAccount1.accountNumber!!, toAccountNumber, transferAmount))
            }

            assertEquals("Account with account number : $toAccountNumber cannot be found", exception2.message)
            Unit
        }

        @Test
        fun testTransferMoneyWithInvalidAmount() = runBlocking {

            val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
            val cust2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
            val savedCustomer1 = addCustomer(cust1)
            val savedCustomer2 = addCustomer(cust2)

            val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
            val account2  = Account(0, savedCustomer2.id!!, 300.0, null)
            val savedAccount1 = addAccount(account1)
            val savedAccount2 = addAccount(account2)

            val transferAmount = -10.0

            val exception = assertThrows(InvalidAmountException::class.java) {
                accountService.transferMoney(MoneyTransferRequestDto(savedAccount1.accountNumber!!, savedAccount2.accountNumber!!, transferAmount))
            }

            assertEquals("Transfer Amount: $transferAmount has to be higher than 0", exception.message)
            Unit
        }

        @Test
        fun testTransferMoneyWithInsufficientFunds() = runBlocking {

            val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
            val cust2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
            val savedCustomer1 = addCustomer(cust1)
            val savedCustomer2 = addCustomer(cust2)

            val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
            val account2  = Account(0, savedCustomer2.id!!, 300.0, null)
            val savedAccount1 = addAccount(account1)
            val savedAccount2 = addAccount(account2)

            val transferAmount = 500.0

            val exception = assertThrows(InsufficientFundsException::class.java) {
                accountService.transferMoney(MoneyTransferRequestDto(savedAccount1.accountNumber!!, savedAccount2.accountNumber!!, transferAmount))
            }

            assertEquals("Account with account number: ${savedAccount1.accountNumber} does not have sufficient funds for withdrawal", exception.message)
            Unit
        }
    }


    private fun addCustomer(customer: Customer): Customer {
        return customerService.addCustomer(customer)
    }

    private fun addAccount(account: Account): Account {
        return accountService.createAccount(account)
    }
}