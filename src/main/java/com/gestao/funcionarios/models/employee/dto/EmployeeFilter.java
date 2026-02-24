package com.gestao.funcionarios.models.employee.dto;

import com.gestao.funcionarios.models.employee.enums.GenderEnum;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record EmployeeFilter(
        String name,
        String email,
        String department,
        String role,
        GenderEnum gender,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "admissionDateStart deve ter o formato yyyy-MM-dd")
        String admissionDateStart,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "admissionDateEnd deve ter o formato yyyy-MM-dd")
        String admissionDateEnd,
        Integer page,
        Integer size,
        String sort
) {
    public EmployeeFilter(
            String name,
            String email,
            String department,
            String role,
            GenderEnum gender,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String admissionDateStart,
            String admissionDateEnd,
            Integer page,
            Integer size,
            String sort
    ) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
        this.gender = gender;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.admissionDateStart = admissionDateStart;
        this.admissionDateEnd = admissionDateEnd;
        this.page = (page == null || page < 0) ? 0 : page;
        this.size = (size == null || size <= 0) ? 50 : size;
        this.sort = (sort == null || sort.isEmpty()) ? "name,asc" : sort;
    }
}