package com.gestao.funcionarios.models.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "Rua não pode ser vazia")
        @Size(max = 255, message = "Rua muito longa")
        String street,

        @NotBlank(message = "Número não pode ser vazio")
        @Size(max = 10, message = "Número inválido")
        String number,

        @NotBlank(message = "Bairro não pode ser vazio")
        @Size(max = 100, message = "Bairro muito longo")
        String neighborhood,

        @NotBlank(message = "Cidade não pode ser vazia")
        @Size(max = 100, message = "Cidade muito longa")
        String city,

        @NotBlank(message = "Estado não pode ser vazio")
        @Size(min = 2, max = 2, message = "Estado deve ser UF (ex: SP)")
        String state,

        @NotBlank(message = "CEP não pode ser vazio")
        @Size(min = 8, max = 9, message = "CEP inválido")
        String zipCode,

        @Size(max = 100, message = "País muito longo")
        String country
) {
}