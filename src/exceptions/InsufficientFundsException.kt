package com.example.exceptions

import java.lang.RuntimeException

class InsufficientFundsException(message: String) : RuntimeException(message)