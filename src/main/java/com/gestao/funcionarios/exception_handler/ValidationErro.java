package com.gestao.funcionarios.exception_handler;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ValidationErro extends ErroResponse {
    private List<FieldMessage> fieldMessageList = new ArrayList<>();

    public ValidationErro(Instant timestamp, Integer code, String path) {
        super(timestamp, code, path);
    }

    public void addErro(String field, String message) {
        this.fieldMessageList.add(new FieldMessage(field, message));
    }

    public String getErroMessage() {
        StringBuilder sb = new StringBuilder();
        for (FieldMessage fieldMessage : fieldMessageList) {
            sb.append(fieldMessage.getMessage()).append(" ");
        }
        return sb.toString().trim();
    }
}