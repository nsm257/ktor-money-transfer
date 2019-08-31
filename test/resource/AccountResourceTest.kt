package resource

import com.example.dto.Account
import com.example.dto.Customer
import com.example.dto.MoneyTransferRequestDto
import com.example.dto.MoneyTransferResponseDto
import common.ServerTest
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AccountResourceTest : ServerTest() {

    @Test
    fun testAddAccount() = runBlocking {
        // given
        val customer = Customer(0, "John Doe", 25, "London", "913567890")
        val savedCustomer = addCustomer(customer)

        // when
        val account = Account(0, savedCustomer.id!!, 30.0, null)
        val newAccount = addAccount(account)

        // then
        val retrieved = get("/accounts/{accountNumber}", newAccount.accountNumber)
            .then()
            .extract().to<Account>()

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
        val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
        val account2  = Account(0, savedCustomer2.id!!, 300.0, null)
        val savedAccount1 = addAccount(account1)
        val savedAccount2 = addAccount(account2)

        // when
        val accounts = get("/accounts/all")
            .then()
            .statusCode(200)
            .extract().to<List<Account>>()

        // then
        assertThat(accounts).hasSize(2)
        assertThat(accounts).extracting("customerId").containsExactlyInAnyOrder(savedAccount1.customerId.toInt(), savedAccount2.customerId.toInt())
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
        val moneyTransferResponse = transferMoney(MoneyTransferRequestDto(savedAccount1.accountNumber!!, savedAccount2.accountNumber!!, transferAmount))
        val retrieved1 = get("/accounts/{accountNumber}", savedAccount1.accountNumber)
            .then()
            .extract().to<Account>()

        val retrieved2 = get("/accounts/{accountNumber}", savedAccount2.accountNumber)
            .then()
            .extract().to<Account>()

        // then
        assertThat(moneyTransferResponse.fromAccount.id).isEqualTo(savedAccount1.id)
        assertThat(moneyTransferResponse.fromAccount.accountNumber).isEqualTo(savedAccount1.accountNumber)
        assertThat(moneyTransferResponse.fromAccount.amount).isEqualTo(savedAccount1.amount - transferAmount)
        assertThat(moneyTransferResponse.fromAccount.customerId).isEqualTo(savedAccount1.customerId)

        assertThat(moneyTransferResponse.toAccount.id).isEqualTo(savedAccount2.id)
        assertThat(moneyTransferResponse.toAccount.accountNumber).isEqualTo(savedAccount2.accountNumber)
        assertThat(moneyTransferResponse.toAccount.amount).isEqualTo(savedAccount2.amount + transferAmount)
        assertThat(moneyTransferResponse.toAccount.customerId).isEqualTo(savedAccount2.customerId)

        assertThat(retrieved1).isEqualTo(moneyTransferResponse.fromAccount)
        assertThat(retrieved2).isEqualTo(moneyTransferResponse.toAccount)

        Unit
    }

    @Nested
    inner class ErrorCases {

        @Test
        fun testCreateAccountWithInvalidCustomer() = runBlocking {

            val account = Account(0, 500, 25.0, null)
            val message = given()
                .contentType(ContentType.JSON)
                .body(account)
                .When()
                .post("/accounts/")
                .then()
                .statusCode(404)

            Unit
        }

        @Test
        fun testCreateAccountWithInvalidAmount() = runBlocking {

            val customer = Customer(0, "John Doe", 25, "London", "1234567890")
            val savedCustomer = addCustomer(customer)
            val account = Account(0, savedCustomer.id!!, -25.0, null)

            val message = given()
                .contentType(ContentType.JSON)
                .body(account)
                .When()
                .post("/accounts/")
                .then()
                .statusCode(400)
                .body(equalTo("Account balance can not be less than 0"))

            Unit
        }

        @Test
        fun testGetAccountWithInvalidAccountNumber() = runBlocking {

            val accountNumber = "abc"
            val message = get("/accounts/{accountNumber}", "abc")
                .then()
                .statusCode(404)
                .body(equalTo("Account with account number : $accountNumber cannot be found"))

            Unit
        }

        @Test
        fun testTransferMoneyWithInvalidAccount() = runBlocking {

            val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
            val savedCustomer1 = addCustomer(cust1)

            val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
            val savedAccount1 = addAccount(account1)

            val transferAmount = 10.0
            val toAccountNumber = "abc"

            val moneyTransferRequestDto1 =
                MoneyTransferRequestDto(savedAccount1.accountNumber!!, toAccountNumber, transferAmount)

            val message1 = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(moneyTransferRequestDto1)
                .When()
                .post("/accounts/transferMoney/")
                .then()
                .log().ifError()
                .statusCode(404)
                .body(equalTo("Account with account number : $toAccountNumber cannot be found"))


            val fromAccountNumber = "def"
            val moneyTransferRequestDto2 =
                MoneyTransferRequestDto(fromAccountNumber, savedAccount1.accountNumber!!, transferAmount)

            val message2 = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(moneyTransferRequestDto2)
                .When()
                .post("/accounts/transferMoney/")
                .then()
                .log().ifError()
                .statusCode(404)
                .body(equalTo("Account with account number : $fromAccountNumber cannot be found"))

            Unit
        }

        @Test
        fun testTransferMoneyWithInvalidAmount() = runBlocking {

            val cust1 = Customer(0, "John Doe", 25, "London", "1234567890")
            val cust2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
            val savedCustomer1 = addCustomer(cust1)
            val savedCustomer2 = addCustomer(cust2)

            val account1 = Account(0, savedCustomer1.id!!, 250.0, null)
            val account2 = Account(0, savedCustomer2.id!!, 300.0, null)
            val savedAccount1 = addAccount(account1)
            val savedAccount2 = addAccount(account2)

            val transferAmount = -10.0

            val moneyTransferRequestDto =
                MoneyTransferRequestDto(savedAccount1.accountNumber!!, savedAccount2.accountNumber!!, transferAmount)

            val message = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(moneyTransferRequestDto)
                .When()
                .post("/accounts/transferMoney/")
                .then()
                .log().ifError()
                .statusCode(400)
                .body(equalTo("Transfer Amount: $transferAmount has to be higher than 0"))

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

            val moneyTransferRequestDto =
                MoneyTransferRequestDto(savedAccount1.accountNumber!!, savedAccount2.accountNumber!!, transferAmount)

            val message = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(moneyTransferRequestDto)
                .When()
                .post("/accounts/transferMoney/")
                .then()
                .log().ifError()
                .statusCode(400)
                .body(equalTo("Account with account number: ${savedAccount1.accountNumber} does not have sufficient funds for withdrawal"))

            Unit
        }
    }

    private fun addCustomer(customer: Customer): Customer {
        return given()
            .contentType(ContentType.JSON)
            .body(customer)
            .When()
            .post("/customers/")
            .then()
            .statusCode(201)
            .extract().to()
    }

    private fun addAccount(account: Account): Account {
        return given()
            .contentType(ContentType.JSON)
            .body(account)
            .When()
            .post("/accounts/")
            .then()
            .statusCode(201)
            .extract().to()
    }

    private fun transferMoney(moneyTransferRequestDto: MoneyTransferRequestDto): MoneyTransferResponseDto {
        return given()
            .log().all()
            .contentType(ContentType.JSON)
            .body(moneyTransferRequestDto)
            .When()
            .post("/accounts/transferMoney/")
            .then()
            .log().ifError()
            .statusCode(200)
            .extract().to()
    }
}