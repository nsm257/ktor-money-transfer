package com.example.resource

import com.example.dto.Account
import com.example.dto.MoneyTransferRequestDto
import com.example.service.AccountService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.accounts(accountService: AccountService) {

    route("/accounts") {

        post("/") {
            val newAccount = call.receive<Account>()
            call.respond(HttpStatusCode.Created, accountService.createAccount(newAccount))
        }

        get("/{accountNumber}") {
            val accountNumber = call.parameters["accountNumber"] ?: throw IllegalStateException("Must provide account number")
            call.respond(HttpStatusCode.OK, accountService.getAccount(accountNumber))
        }

        post("/transferMoney") {
            val moneyTransferDto = call.receive<MoneyTransferRequestDto>()
            call.respond(HttpStatusCode.OK, accountService.transferMoney(moneyTransferDto))
        }

        get("/all") {
            call.respond(HttpStatusCode.OK, accountService.getAllAccounts())
        }
    }
}