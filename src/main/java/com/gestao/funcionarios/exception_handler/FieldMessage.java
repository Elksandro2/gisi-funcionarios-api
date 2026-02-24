package com.gestao.funcionarios.exception_handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FieldMessage {
    private String field;
    private String message;
}