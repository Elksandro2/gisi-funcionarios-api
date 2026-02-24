package com.gestao.funcionarios.exception_handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErroResponse {
    private Instant timestamp;
    private Integer code;
    private String message;
    private String path;

    public ErroResponse(Instant timestamp, Integer code, String path) {
        this.timestamp = timestamp;
        this.code = code;
        this.path = path;
    }

    public ErroResponse(String message) {
        this.message = message;
    }
}