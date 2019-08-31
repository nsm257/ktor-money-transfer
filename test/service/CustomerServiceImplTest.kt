package service


import com.example.dto.Customer
import com.example.exceptions.CustomerNotFoundException
import com.example.exceptions.InvalidAgeException
import com.example.service.CustomerService
import com.example.service.CustomerServiceImpl
import common.ServerTest
//import common.ServerTest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class CustomerServiceImplTest: ServerTest() {

    private val customerService = CustomerServiceImpl()

    @Test
    fun testAddCustomer() = runBlocking {
        // given
        val customer1 = Customer(0, "John Doe", 25, "London", "1234567890")

        // when
        val saved = addCustomer(customer1)

        // then
        val retrieved = customerService.getCustomer(saved.id!!)
        assertThat(retrieved).isEqualTo(saved)
        assertThat(retrieved.name).isEqualTo(customer1.name)
        assertThat(retrieved.age).isEqualTo(customer1.age)
        assertThat(retrieved.city).isEqualTo(customer1.city)
        assertThat(retrieved.phoneNumber).isEqualTo(customer1.phoneNumber)

        Unit
    }

    @Test
    fun testGetAllCustomers() = runBlocking {
        // given
        val customer1 = Customer(0, "John Doe", 25, "London", "1234567890")
        val customer2 = Customer(0, "Jane Dyre", 28, "Edgebeston", "9876532130")
        addCustomer(customer1)
        addCustomer(customer2)

        // when
        val customers = customerService.getAllCustomers()

        // then
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
        fun testAddCustomerWithInvalidAge() = runBlocking {

            val exception = assertThrows(InvalidAgeException::class.java) {
                customerService.addCustomer(Customer(0, "John Doe", -25, "London", "1234567890"))
            }

            assertEquals("Customer age must be greater than 0", exception.message)
            Unit
        }

        @Test
        fun testGetInvalidCustomer() = runBlocking {

            val custId = 1L

            val exception = assertThrows(CustomerNotFoundException::class.java) {
                customerService.getCustomer(custId)
            }

            assertEquals("Customer with Id: $custId cannot be found", exception.message)
            Unit
        }
    }

    private suspend fun addCustomer(customer: Customer): Customer {
        return customerService.addCustomer(customer)
    }
}