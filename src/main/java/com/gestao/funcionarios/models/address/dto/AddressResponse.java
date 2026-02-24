package com.gestao.funcionarios.models.address.dto;

public record AddressResponse(
    String street,
    String number,
    String neighborhood,
    String city,
    String state,
    String zipCode,
    String country
) {}