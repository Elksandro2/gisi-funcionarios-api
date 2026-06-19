package com.gestao.funcionarios.models.chatbot.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.gestao.funcionarios.models.chatbot.dto.ChatRequest;
import com.gestao.funcionarios.models.chatbot.dto.ChatResponse;
import com.gestao.funcionarios.models.employee.entity.Employee;
import com.gestao.funcionarios.models.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatService {

    private final EmployeeRepository employeeRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String apiModel;

    public ChatService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.restTemplate = new RestTemplate();
    }

    public ChatResponse askAssistant(ChatRequest request) {
        long totalEmployees = employeeRepository.count();
        List<Employee> employees = employeeRepository.findAll().stream().limit(50).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Nome|Cargo|Depto|Salario\n");
        for (Employee e : employees) {
            sb.append(e.getName() != null ? e.getName() : "").append("|")
              .append(e.getRole() != null ? e.getRole() : "").append("|")
              .append(e.getDepartment() != null ? e.getDepartment() : "").append("|")
              .append(e.getSalary() != null ? e.getSalary().intValue() : "0")
              .append("\n");
        }

        String systemPrompt = "Você é o assistente do sistema GISI.\n" +
                "DADO REAL PARA CÁLCULOS: A empresa possui atualmente " + totalEmployees + " funcionários no total.\n" +
                "Abaixo está uma amostra de contexto. Responda de forma direta e curta.\n" +
                "Amostra:\n" + sb.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        GroqRequest groqRequest = new GroqRequest(
                apiModel,
                List.of(
                        new GroqMessage("system", systemPrompt),
                        new GroqMessage("user", request.message())
                )
        );

        HttpEntity<GroqRequest> httpEntity = new HttpEntity<>(groqRequest, headers);

        try {
            // Consome como Map para ignorar qualquer propriedade extra do Gemini
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, httpEntity, Map.class);
            Map<?, ?> body = responseEntity.getBody();
            
            if (body != null && body.containsKey("choices")) {
                List<?> choices = (List<?>) body.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    return new ChatResponse(content);
                }
            }
            return new ChatResponse("A IA não retornou resposta válida.");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Erro retornado pela API do Gemini ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Falha na API de IA: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Erro inesperado no processamento da IA: ", e);
            throw new IllegalStateException("Falha na comunicação com o assistente.");
        }
    }

    public record GroqRequest(String model, List<GroqMessage> messages) {}
    public record GroqMessage(String role, String content) {}
    public record GroqResponse(List<GroqChoice> choices) {}
    public record GroqChoice(GroqMessage message) {}
}