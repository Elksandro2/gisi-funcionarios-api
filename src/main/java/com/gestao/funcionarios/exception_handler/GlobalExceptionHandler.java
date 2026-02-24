package com.gestao.funcionarios.exception_handler;

import com.gestao.funcionarios.models.employee.service.exception.EmployeeBusinessException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validationError(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationErro validationErro = new ValidationErro(Instant.now(), status.value(), request.getRequestURI());
        
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            validationErro.addErro(error.getField(), error.getDefaultMessage());
        }
        
        validationErro.setMessage("Erro de validação nos campos: " + validationErro.getErroMessage());
        log.warn("MethodArgumentNotValidException: {}", validationErro.getErroMessage());
        return ResponseEntity.status(status).body(validationErro);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResponse> entityNotFoundError(EntityNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErroResponse erroResponse = new ErroResponse(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        log.warn("EntityNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(status).body(erroResponse);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErroResponse> entityExistsError(EntityExistsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponse erroResponse = new ErroResponse(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        log.warn("EntityExistsException: {}", e.getMessage());
        return ResponseEntity.status(status).body(erroResponse);
    }

    @ExceptionHandler(EmployeeBusinessException.class)
    public ResponseEntity<ErroResponse> employeeBusinessError(EmployeeBusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroResponse erroResponse = new ErroResponse(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        log.error("EmployeeBusinessException: {}", e.getMessage());
        return ResponseEntity.status(status).body(erroResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> illegalArgumentError(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroResponse erroResponse = new ErroResponse(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.status(status).body(erroResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> httpMessageNotReadableError(HttpMessageNotReadableException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroResponse erroResponse = new ErroResponse(Instant.now(), status.value(), "Corpo da requisição inválido ou mal formatado", request.getRequestURI());
        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        return ResponseEntity.status(status).body(erroResponse);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ErroResponse> dateTimeError(DateTimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroResponse erroResponse = new ErroResponse(Instant.now(), status.value(), "Erro no formato de data: " + e.getMessage(), request.getRequestURI());
        log.warn("DateTimeException: {}", e.getMessage());
        return ResponseEntity.status(status).body(erroResponse);
    }
}