package service

import com.example.dto.Account
import com.example.dto.Customer
import com.example.dto.MoneyTransferRequestDto
import com.example.dto.MoneyTransferResponseDto
import com.example.service.AccountService
import com.example.service.AccountServiceImpl
import com.example.service.CustomerServiceImpl
import common.ServerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.ArrayList
import java.util.concurrent.*


class ConcurrentTransferTest : ServerTest() {

    private val NUM_OF_MONEY_TRANSFERS = 1000

    private val accountService = AccountServiceImpl()
    private val customerService = CustomerServiceImpl()

    @Test
    @Throws(InterruptedException::class)
    fun ensuresThreadSafetyForConcurrentTransfers() {
        val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
        val cust2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
        val savedCustomer1 = addCustomer(cust1)
        val savedCustomer2 = addCustomer(cust2)

        val account1 = Account(0, savedCustomer1.id!!, 10000.50, null)
        val account2 = Account(0, savedCustomer2.id!!, 50000.50, null)
        val fromAccount = addAccount(account1)
        val toAccount = addAccount(account2)

        val tasks = ArrayList<Callable<MoneyTransferResponseDto>>(NUM_OF_MONEY_TRANSFERS)

        for (i in 0 until NUM_OF_MONEY_TRANSFERS) {

            tasks.add(Callable {
                accountService.transferMoney(
                    MoneyTransferRequestDto(
                        fromAccount.accountNumber!!,
                        toAccount.accountNumber!!,
                        1.0
                    )
                )
            })
            tasks.add(Callable {
                accountService.transferMoney(
                    MoneyTransferRequestDto(
                        toAccount.accountNumber!!,
                        fromAccount.accountNumber!!,
                        2.0
                    )
                )
            })
        }

        val es: ExecutorService = Executors.newFixedThreadPool(4)
        es.invokeAll(tasks)

        es.shutdown()
        es.awaitTermination(1, TimeUnit.MINUTES)
        sleep(1000)
        assertThat(accountService.getAccount(fromAccount.accountNumber!!).amount).isEqualTo(fromAccount.amount + NUM_OF_MONEY_TRANSFERS)
        assertThat(accountService.getAccount(toAccount.accountNumber!!).amount).isEqualTo(toAccount.amount - NUM_OF_MONEY_TRANSFERS)
        Unit
    }

    private fun addCustomer(customer: Customer): Customer {
        return customerService.addCustomer(customer)
    }

    private fun addAccount(account: Account): Account {
        return accountService.createAccount(account)
    }
}