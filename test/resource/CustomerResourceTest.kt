package resource

import com.example.dto.Customer
import com.example.model.Accounts
import com.example.model.Customers

import common.ServerTest
import org.junit.jupiter.api.Test
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.AfterEach


class CustomerResourceTest : ServerTest() {

    @Test
    fun testAddCustomer() {
        // when
        val newCustomer = Customer(0, "Johnny Doe", 26, "London", "1367890")
        val created = addCustomer(newCustomer)

        val retrieved = get("/customers/{id}", created.id)
            .then()
            .extract().to<Customer>()

        // then
        assertThat(created.name).isEqualTo(newCustomer.name)
        assertThat(created.age).isEqualTo(newCustomer.age)
        assertThat(created.city).isEqualTo(newCustomer.city)
        assertThat(created.phoneNumber).isEqualTo(newCustomer.phoneNumber)

        assertThat(created).isEqualTo(retrieved)
        Unit
    }

    @Test
    fun testGetAllCustomers() {
        // when
        val customer1 = Customer(0, "John Doe", 25, "London", "1234567890")
        val customer2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "986532130")
        addCustomer(customer1)
        addCustomer(customer2)

        val customers = get("/customers/all")
            .then()
            .statusCode(200)
            .extract().to<List<Customer>>()

        assertThat(customers).hasSize(2)
        assertThat(customers).extracting("name").containsExactlyInAnyOrder(customer1.name, customer2.name)
        assertThat(customers).extracting("age").containsExactlyInAnyOrder(customer1.age, customer2.age)
        assertThat(customers).extracting("city").containsExactlyInAnyOrder(customer1.city, customer2.city)
        assertThat(customers).extracting("phoneNumber").containsExactlyInAnyOrder(customer1.phoneNumber, customer2.phoneNumber)
        Unit
    }

    @Nested
    inner class ErrorCases {

        @Test
        fun testGetInvalidCustomer() {

            val custId = 1L
            get("/customers/{id}", custId)
                .then()
                .statusCode(404)
                .body(equalTo("Customer with Id: $custId cannot be found"))
            Unit
        }

        @Test
        fun testAddCustomerWithInvalidAge() {

            val customer = Customer(0, "John Doe", -25, "London", "1234567890")

            val received = given()
                .contentType(ContentType.JSON)
                .body(customer)
                .When()
                .post("/customers/")
                .then()
                .statusCode(400)
                .body(equalTo("Customer age must be greater than 0"))

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

}