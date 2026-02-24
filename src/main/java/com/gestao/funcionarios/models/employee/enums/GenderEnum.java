package com.gestao.funcionarios.models.employee.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    MASCULINO("Masculino"),
    FEMININO("Feminino"),
    OUTRO("Outro");

    private final String description;

    GenderEnum(String description) {
        this.description = description;
    }
}