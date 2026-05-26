package com.gestao.funcionarios.models.chatbot.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestao.funcionarios.models.chatbot.dto.ChatRequest;
import com.gestao.funcionarios.models.chatbot.dto.ChatResponse;
import com.gestao.funcionarios.models.employee.dto.EmployeeResponse;
import com.gestao.funcionarios.models.employee.entity.Employee;
import com.gestao.funcionarios.models.employee.repository.EmployeeRepository;

@Service
public class ChatService {

    private final EmployeeRepository employeeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String apiModel;

    public ChatService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ChatResponse askAssistant(ChatRequest request) {
        // 1. Buscar os funcionários
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponse> employeeResponses = employees.stream()
                .map(Employee::toResponse)
                .collect(Collectors.toList());

        // Serializar a lista para JSON
        String employeesJson;
        try {
            employeesJson = objectMapper.writeValueAsString(employeeResponses);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Erro ao processar dados dos funcionários para o Chatbot.", e);
        }

        // 2. Montar o System Prompt forte com as regras e os dados reais
        String systemPrompt = "Você é um assistente virtual exclusivo do sistema de gestão de funcionários GISI. " +
                "Responda apenas perguntas sobre os funcionários listados no JSON fornecido. " +
                "Não invente dados e recuse educadamente perguntas fora do escopo do sistema GISI.\n\n" +
                "JSON de Funcionários:\n" + employeesJson;

        // 3. Montar a requisição HTTP seguindo a especificação OpenAI
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
            ResponseEntity<GroqResponse> responseEntity = restTemplate.postForEntity(apiUrl, httpEntity, GroqResponse.class);
            GroqResponse groqResponse = responseEntity.getBody();

            if (groqResponse != null && groqResponse.choices() != null && !groqResponse.choices().isEmpty()) {
                String responseText = groqResponse.choices().get(0).message().content();
                return new ChatResponse(responseText);
            } else {
                throw new IllegalStateException("A API da IA retornou uma resposta sem conteúdo.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha na comunicação com o assistente de IA: " + e.getMessage(), e);
        }
    }

    // Records auxiliares para mapeamento da API da Groq no formato OpenAI
    public record GroqRequest(String model, List<GroqMessage> messages) {}
    public record GroqMessage(String role, String content) {}
    public record GroqResponse(List<GroqChoice> choices) {}
    public record GroqChoice(GroqMessage message) {}
}
