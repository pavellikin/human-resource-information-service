package org.mycompany.hris.exception

class BadRequestException(message: String) : RuntimeException(message)

class InternalException(message: String) : RuntimeException(message)
