package com.gestao.funcionarios.models.employee.dto;

import com.gestao.funcionarios.models.address.dto.AddressRequest;
import com.gestao.funcionarios.models.employee.enums.GenderEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
    @NotBlank(message = "Nome é obrigatório")
    String name,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotNull(message = "Gênero é obrigatório")
    GenderEnum gender,

    @NotBlank(message = "Cargo é obrigatório")
    String role,

    @NotBlank(message = "Departamento é obrigatório")
    String department,

    @NotNull(message = "Salário é obrigatório")
    @Positive(message = "Salário deve ser maior que zero")
    BigDecimal salary,

    @NotNull(message = "Data de admissão é obrigatória")
    @PastOrPresent(message = "A data de admissão não pode ser futura")
    LocalDate admissionDate,

    @NotNull(message = "Endereço é obrigatório")
    @Valid
    AddressRequest address
) {}