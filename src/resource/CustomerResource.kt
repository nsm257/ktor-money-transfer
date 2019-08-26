package com.example.resource

import com.example.dto.Customer
import com.example.service.CustomerService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.customers(customerService: CustomerService) {

    route("/customers") {

        post("/") {
            val newCustomer = call.receive<Customer>()
            call.respond(HttpStatusCode.Created, customerService.addCustomer(newCustomer))
        }

        get("/{id}") {
            val id = call.parameters["id"]!!.toLongOrNull() ?: throw NumberFormatException("Must provide customer id as a Long")
            call.respond(customerService.getCustomer(id))
        }

        get("/all") {
            call.respond(HttpStatusCode.OK, customerService.getAllCustomers())
        }
    }
}