package com.gestao.funcionarios.models.employee.service.exception;

public class EmployeeBusinessException extends RuntimeException {
    public EmployeeBusinessException(String msg) {
        super(msg);
    }
}