package com.gestao.funcionarios.models.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank(message = "A mensagem não pode ser vazia ou nula")
    String message
) {}
