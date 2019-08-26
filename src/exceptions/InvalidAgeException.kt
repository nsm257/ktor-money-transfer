package com.example.exceptions

import java.lang.RuntimeException

class InvalidAgeException(message: String) : RuntimeException(message)