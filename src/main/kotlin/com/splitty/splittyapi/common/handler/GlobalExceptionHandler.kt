package com.splitty.splittyapi.common.handler

import com.splitty.splittyapi.common.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.NoSuchElementException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundException(
        ex: NoSuchElementException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid state",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid argument",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = "Data integrity violation - possible duplicate or constraint violation",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Invalid parameter format: ${ex.name}",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
