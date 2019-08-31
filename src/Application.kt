package com.example

import com.example.exceptions.CustomerNotFoundException
import com.example.exceptions.AccountNotFoundException
import com.example.exceptions.InsufficientFundsException
import com.example.exceptions.InvalidAgeException
import com.example.exceptions.InvalidAmountException
import com.example.model.Accounts
import com.example.model.Customers
import com.example.resource.accounts
import com.example.resource.customers
import com.example.service.AccountServiceImpl
import com.example.service.CustomerServiceImpl
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.*
import io.ktor.routing.Routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Accounts, Customers)
    }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    install(DefaultHeaders)
    install(CallLogging)

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    install(StatusPages) {
        exception<CustomerNotFoundException> { cause ->
            call.respond(HttpStatusCode.NotFound, cause.message!!)
        }
        exception<AccountNotFoundException> { cause ->
            call.respond(HttpStatusCode.NotFound, cause.message!!)
        }
        exception<InsufficientFundsException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message!!)
        }
        exception<InvalidAmountException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message!!)
        }
        exception<InvalidAgeException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message!!)
        }
    }

    install(Routing) {
        accounts(AccountServiceImpl())
        customers(CustomerServiceImpl())
    }
}
