package com.example.exceptions

import java.lang.RuntimeException

class InvalidAmountException(message: String) : RuntimeException(message)