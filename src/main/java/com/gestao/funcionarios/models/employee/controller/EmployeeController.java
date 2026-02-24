package com.gestao.funcionarios.models.employee.controller;

import com.gestao.funcionarios.models.employee.dto.EmployeeFilter;
import com.gestao.funcionarios.models.employee.dto.EmployeeRequest;
import com.gestao.funcionarios.models.employee.dto.EmployeeResponse;
import com.gestao.funcionarios.models.employee.dto.EmployeeStats;
import com.gestao.funcionarios.models.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/employees")
@Tag(name = "Employee", description = "Gestão de Funcionários e Indicadores Gerenciais")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Listar funcionários com filtros", responses = {
            @ApiResponse(responseCode = "200", description = "Funcionários listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<EmployeeResponse>> findAllEmployees(@ModelAttribute @Valid EmployeeFilter filter) {
        Pageable pageable = createPageable(filter);
        return ResponseEntity.ok(employeeService.findAllEmployees(filter, pageable));
    }

    @Operation(summary = "Buscar funcionário por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findEmployeeById(id));
    }

    @Operation(summary = "Obter estatísticas globais dos funcionários", responses = {
            @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/stats")
    public ResponseEntity<EmployeeStats> findStats() {
        return ResponseEntity.ok(employeeService.findStats());
    }

    @Operation(summary = "Cadastrar novo funcionário", responses = {
            @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflito: Email já cadastrado")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @Operation(summary = "Atualizar dados do funcionário", responses = {
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @RequestBody @Valid EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @Operation(summary = "Remover funcionário do sistema", responses = {
            @ApiResponse(responseCode = "204", description = "Funcionário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(EmployeeFilter filter) {
        String[] sortParams = filter.sort().split(",");
        Sort sortBy = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        return PageRequest.of(filter.page(), filter.size(), sortBy);
    }
}