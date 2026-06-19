package com.gestao.funcionarios.models.chatbot.service;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestao.funcionarios.models.chatbot.dto.ChatRequest;
import com.gestao.funcionarios.models.chatbot.dto.ChatResponse;
import com.gestao.funcionarios.models.chatbot.service.ChatService.GroqMessage;
import com.gestao.funcionarios.models.chatbot.service.ChatService.GroqResponse;
import com.gestao.funcionarios.models.employee.entity.Employee;
import com.gestao.funcionarios.models.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatService {

    private final EmployeeRepository employeeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String apiModel;

    public ChatService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ChatResponse askAssistant(ChatRequest request) {
        // 1. Obter estatísticas agregadas reais do banco de dados
        EmployeeRepository.EmployeeSummary summary = employeeRepository.getGlobalSummary();
        List<EmployeeRepository.ChartProjection> deptDistribution = employeeRepository.getDeptDistribution();

        long totalEmployees = summary.getTotalEmployees();
        BigDecimal totalSalary = summary.getTotalSalary();
        double avgSalaryVal = summary.getAverageSalary() != null ? summary.getAverageSalary() : 0.0;
        BigDecimal averageSalary = BigDecimal.valueOf(avgSalaryVal).setScale(2, RoundingMode.HALF_UP);

        StringBuilder deptSb = new StringBuilder();
        for (EmployeeRepository.ChartProjection proj : deptDistribution) {
            if (deptSb.length() > 0) {
                deptSb.append(", ");
            }
            deptSb.append(proj.getName()).append(": ").append(proj.getValue());
        }
        String deptDistributionStr = deptSb.toString();

        // 2. Buscar amostra limitada de funcionários para contexto individual
        List<Employee> employees = employeeRepository.findAll().stream().limit(50).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Nome|G|Cargo|Depto|Salario|Admissao\n");
        for (Employee e : employees) {
            String genderCode = "";
            if (e.getGender() != null) {
                genderCode = e.getGender().name().startsWith("M") ? "M" : "F";
            }
            sb.append(e.getName() != null ? e.getName() : "").append("|")
                    .append(genderCode).append("|")
                    .append(e.getRole() != null ? e.getRole() : "").append("|")
                    .append(e.getDepartment() != null ? e.getDepartment() : "").append("|")
                    .append(e.getSalary() != null ? e.getSalary().intValue() : "0").append("|")
                    .append(e.getAdmissionDate() != null ? e.getAdmissionDate().toString() : "")
                    .append("\n");
        }
        String employeesPsv = sb.toString();

        // 3. Montar o System Prompt
        String systemPrompt = "Você é um assistente virtual exclusivo do sistema de gestão de funcionários GISI. " +
                "Não invente dados e recuse educadamente perguntas fora do escopo do sistema GISI.\n\n" +
                "ESTATÍSTICAS GLOBAIS DA EMPRESA (REAIS E ATUALIZADAS):\n" +
                "- Total de funcionários cadastrados: " + totalEmployees + "\n" +
                "- Média Salarial Global: R$ " + averageSalary + "\n" +
                "- Total da Folha de Pagamento: R$ " + totalSalary + "\n" +
                "- Distribuição de Funcionários por Departamento: [" + deptDistributionStr + "]\n\n" +
                "Sempre que perguntarem sobre totais de funcionários, médias salariais gerais ou distribuições de departamentos, " +
                "use estritamente os dados da seção ESTATÍSTICAS GLOBAIS para responder com precisão matemática absoluta.\n\n" +
                "Regras estritas de resposta:\n" +
                "1. Seja extremamente conciso, direto e amigável. Responda diretamente à pergunta sem explicações longas ou textos desnecessários.\n" +
                "2. Use os dados da seção de estatísticas globais para qualquer questão agregada global.\n" +
                "3. Use a coluna G (M para masculino, F para feminino) para responder sobre gênero de forma precisa.\n" +
                "4. Se o usuário solicitar distribuições, comparações, médias de departamentos ou relatórios estatísticos, gere um gráfico interativo inserindo a tag XML abaixo no meio da sua resposta:\n" +
                "   <chart type=\"bar|pie\" title=\"Título do Gráfico\">\n" +
                "   [{\"name\": \"Nome 1\", \"value\": 10}, {\"name\": \"Nome 2\", \"value\": 5}]\n" +
                "   </chart>\n" +
                "   Use type=\"bar\" para distribuições gerais e type=\"pie\" para proporções pequenas.\n\n" +
                "Amostra de Funcionários para Contexto de Cargos/Nomes (Nome|G|Cargo|Departamento|Salario|Admissao):\n" + employeesPsv;

        // 3. Montar a requisição HTTP para a API do DeepSeek
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        GroqRequest groqRequest = new GroqRequest(
                apiModel,
                List.of(
                        new GroqMessage("system", systemPrompt),
                        new GroqMessage("user", request.message())));

        HttpEntity<GroqRequest> httpEntity = new HttpEntity<>(groqRequest, headers);

        try {
            ResponseEntity<GroqResponse> responseEntity = restTemplate.postForEntity(apiUrl, httpEntity,
                    GroqResponse.class);
            GroqResponse groqResponse = responseEntity.getBody();

            if (groqResponse != null && groqResponse.choices() != null && !groqResponse.choices().isEmpty()) {
                String responseText = groqResponse.choices().get(0).message().content();
                return new ChatResponse(responseText);
            } else {
                throw new IllegalStateException("A API do DeepSeek retornou uma resposta vazia.");
            }
        } catch (HttpClientErrorException e) {
            // Log sênior: captura o corpo real do erro retornado pelo provedor (ex: saldo
            // insuficiente, limite de tokens)
            log.error("Erro retornado pela API de IA (Status {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException(
                    "Falha na API de IA: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro inesperado na comunicação com a IA: ", e);
            throw new IllegalStateException("Falha na comunicação com o assistente de IA: " + e.getMessage(), e);
        }
    }

    public record GroqRequest(String model, List<GroqMessage> messages) {
    }

    public record GroqMessage(String role, String content) {
    }

    public record GroqResponse(List<GroqChoice> choices) {
    }

    public record GroqChoice(GroqMessage message) {
    }
}