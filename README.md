# ktor-money-transfer
Ktor demo project for building a simple money tansfer application

## Overview
Simple Application to demonstrate money transfer between internal accounts, using Kotlin with Ktor Framework, 'Exposed' as ORM and H2 as in memory database

## Endpoints
* `Post` /customers - Create new Customer
    - example request body
    ```json 
    {
    	"id": 0,
    	"name": "John Doe",
    	"age" : 22,
    	"city": "London",
    	"phoneNumber" : "9927621534"
    }
    ```
* `Get` /customers/&lt;customerId&gt; - Returns the specified customer info
* `Get` /customers/all - Returns all customers' info
* `Post` /accounts - Create new Account
    - example request body
    ```json 
    {
    	"id": 0,
    	"customerId": 1,
    	"amount": 1000
    }
    ```
* `Get` /accounts/&lt;accountNumber&gt; - Returns the specified account info
* `Get` /accounts/all - Returns all accounts' info
* `Post` /accounts/transferMoney - Transfer money from one account to another
    - example request body
    ```json 
    {
    	"fromAccountNumber": "94569780-e8f5-4250-b765-128c0a07577a",
    	"toAccountNumber": "028fa2e2-86bf-4a0f-97ab-47ac0412d031",
    	"amount": 1000
    }
    ```

## How to Run the application
From the root directory
```
./gradlew run
```

Run tests
```
./gradlew test run
```

Run tests with coverage
```
./gradlew test jacocoTestReport run
```

Shortcut (to run the application with tests and coverage)
```
./r
```

In all the above examples, the application starts on following host:port - [http://localhost:7777](http://localhost:7777)

## Steps to test "transfer money" application (manually)

1. Create customer(s)
2. Create two accounts passing on the customer id(s) created in the previous step
3. Transfer money using account numbers created in the previous step.
