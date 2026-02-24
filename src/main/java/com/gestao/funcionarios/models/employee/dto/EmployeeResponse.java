package com.gestao.funcionarios.models.employee.dto;

import com.gestao.funcionarios.models.address.dto.AddressResponse;
import com.gestao.funcionarios.models.employee.enums.GenderEnum;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
    Long id,
    String name,
    String email,
    GenderEnum gender,
    String role,
    String department,
    BigDecimal salary,
    LocalDate admissionDate,
    AddressResponse address
) {}