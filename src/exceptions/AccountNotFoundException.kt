package com.example.exceptions

import java.lang.RuntimeException

class AccountNotFoundException(message: String) : RuntimeException(message)