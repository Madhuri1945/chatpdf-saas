
package com.example.chatpdf.utils;

import com.example.chatpdf.exceptions.auth.DuplicateEmailException;
import com.example.chatpdf.exceptions.auth.InvalidCredentialsException;
import com.example.chatpdf.exceptions.auth.InvalidTokenException;
import com.example.chatpdf.exceptions.auth.RefreshTokenExpiredException;
import com.example.chatpdf.exceptions.common.DocumentNotReadyException;
import com.example.chatpdf.exceptions.common.ResourceNotFoundException;

import com.example.chatpdf.document.exception.CorruptedPdfException;
import com.example.chatpdf.document.exception.DocumentNotFoundException;
import com.example.chatpdf.document.exception.EmptyPdfException;
import com.example.chatpdf.document.exception.FileTooLargeException;
import com.example.chatpdf.document.exception.InvalidFileException;

import com.example.chatpdf.exceptions.llm.LLMGatewayException;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(ApiResponse.error(message));
    }

    // Authentication exceptions

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(
            DuplicateEmailException ex) {

        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({
            InvalidTokenException.class,
            RefreshTokenExpiredException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokens(
            RuntimeException ex) {

        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Resource not found

    @ExceptionHandler({
            ResourceNotFoundException.class,
            DocumentNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            RuntimeException ex) {

        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // PDF processing exceptions

    @ExceptionHandler({
            CorruptedPdfException.class,
            EmptyPdfException.class,
            InvalidFileException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidPdf(
            RuntimeException ex) {

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileTooLarge(
            FileTooLargeException ex) {

        return error(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage());
    }

    // Request validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": "
                        + e.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(
            HttpMessageNotReadableException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Invalid or missing request body"
        );
    }

    // Database integrity errors

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        log.error("Database integrity violation", ex);

        return error(
                HttpStatus.CONFLICT,
                "The request conflicts with existing data"
        );
    }

    // External service errors

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalService(
            RestClientException ex) {

        log.error("External service request failed", ex);

        return error(
                HttpStatus.BAD_GATEWAY,
                "An external service failed to process the request"
        );
    }

    @ExceptionHandler(LLMGatewayException.class)
    public ResponseEntity<ApiResponse<Void>> handleLLMGatewayException(
            LLMGatewayException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(
                        "The AI service is temporarily unavailable. Please try again."
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Unexpected errors
    public ResponseEntity<ApiResponse<Void>> handleDocumentNotReady(DocumentNotReadyException ex){
        ApiResponse<Void> response=new ApiResponse<>(
                false,ex.getMessage(),null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex) {

        log.error("Unexpected application error", ex);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }



}
