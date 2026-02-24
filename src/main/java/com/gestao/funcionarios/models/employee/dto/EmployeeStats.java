package com.gestao.funcionarios.models.employee.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmployeeStats(
    long totalEmployees,
    BigDecimal totalSalary,
    Double averageSalary,
    List<CountDTO> genderDist,
    List<CountDTO> deptDist,
    List<CountDTO> cityDist,
    List<CountDTO> yearDist
) {
    public record CountDTO(String name, Long value) {}
}