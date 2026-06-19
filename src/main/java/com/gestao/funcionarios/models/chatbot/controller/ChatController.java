package com.gestao.funcionarios.models.chatbot.controller;

import com.gestao.funcionarios.models.chatbot.dto.ChatRequest;
import com.gestao.funcionarios.models.chatbot.dto.ChatResponse;
import com.gestao.funcionarios.models.chatbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/assistant")
@Tag(name = "Chatbot IA", description = "Endpoint para comunicação com o assistente virtual do GISI")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Enviar pergunta para o assistente IA", responses = {
            @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição mal formatada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ou na API de IA")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> askAssistant(@RequestBody @Valid ChatRequest request) {
        ChatResponse response = chatService.askAssistant(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ChatResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(500).body(new ChatResponse("Erro no Assistente: " + ex.getMessage()));
    }
}
