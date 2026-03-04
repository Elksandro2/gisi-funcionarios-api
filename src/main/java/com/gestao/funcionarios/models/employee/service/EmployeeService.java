package com.gestao.funcionarios.models.employee.service;

import com.gestao.funcionarios.models.address.entity.Address;
import com.gestao.funcionarios.models.employee.dto.EmployeeFilter;
import com.gestao.funcionarios.models.employee.dto.EmployeeRequest;
import com.gestao.funcionarios.models.employee.dto.EmployeeResponse;
import com.gestao.funcionarios.models.employee.dto.EmployeeStats;
import com.gestao.funcionarios.models.employee.dto.EmployeeStats.CountDTO;
import com.gestao.funcionarios.models.employee.entity.Employee;
import com.gestao.funcionarios.models.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findAllEmployees(EmployeeFilter filter, Pageable pageable) {
        log.info("Buscando funcionários paginados com filtro: {}", filter);
        Specification<Employee> spec = createSpecification(filter);
        return employeeRepository.findAll(spec, pageable).map(Employee::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeStats findStats(EmployeeFilter filter) {
        log.info("Calculando estatísticas dinâmicas para o filtro: {}", filter);

        // 1. Busca TODOS os funcionários que atendem ao filtro (ignorando paginação
        // para o BI)
        Specification<Employee> spec = createSpecification(filter);
        List<Employee> filteredEmployees = employeeRepository.findAll(spec);

        // 2. Cálculos Gerais
        long total = filteredEmployees.size();

        BigDecimal sum = filteredEmployees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double avg = total > 0
                ? sum.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 3. Agrupamentos para os Gráficos
        List<CountDTO> genderDist = filteredEmployees.stream()
                .collect(Collectors.groupingBy(e -> e.getGender().name(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new CountDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<CountDTO> deptDist = filteredEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new CountDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<CountDTO> cityDist = filteredEmployees.stream()
                .collect(Collectors.groupingBy(e -> e.getAddress().getCity(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new CountDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<CountDTO> yearDist = filteredEmployees.stream()
                .collect(Collectors.groupingBy(e -> String.valueOf(e.getAdmissionDate().getYear()),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new CountDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toList());

        return new EmployeeStats(total, sum, avg, genderDist, deptDist, cityDist, yearDist);
    }

    private Specification<Employee> createSpecification(EmployeeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), filter.name().toLowerCase() + "%"));
            }
            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(cb.equal(root.get("email"), filter.email()));
            }
            if (filter.department() != null && !filter.department().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("department")), filter.department().toLowerCase()));
            }
            if (filter.gender() != null) {
                predicates.add(cb.equal(root.get("gender"), filter.gender()));
            }
            if (filter.minSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), filter.minSalary()));
            }
            if (filter.maxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), filter.maxSalary()));
            }
            if (filter.admissionDateStart() != null && !filter.admissionDateStart().isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("admissionDate"),
                        LocalDate.parse(filter.admissionDateStart())));
            }
            if (filter.admissionDateEnd() != null && !filter.admissionDateEnd().isBlank()) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("admissionDate"), LocalDate.parse(filter.admissionDateEnd())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Criando novo funcionário: {}", request.name());

        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado no sistema.");
        }

        Address address = Address.builder()
                .street(request.address().street())
                .number(request.address().number())
                .neighborhood(request.address().neighborhood())
                .city(request.address().city())
                .state(request.address().state())
                .zipCode(request.address().zipCode())
                .country(request.address().country())
                .build();

        Employee employee = Employee.builder()
                .name(request.name())
                .email(request.email())
                .gender(request.gender())
                .role(request.role())
                .department(request.department())
                .salary(request.salary())
                .admissionDate(request.admissionDate())
                .address(address)
                .build();

        return employeeRepository.save(employee).toResponse();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(Employee::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + id));
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Atualizando funcionário ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado"));

        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setGender(request.gender());
        employee.setRole(request.role());
        employee.setDepartment(request.department());
        employee.setSalary(request.salary());
        employee.setAdmissionDate(request.admissionDate());

        Address addr = employee.getAddress();
        addr.setStreet(request.address().street());
        addr.setNumber(request.address().number());
        addr.setNeighborhood(request.address().neighborhood());
        addr.setCity(request.address().city());
        addr.setState(request.address().state());
        addr.setZipCode(request.address().zipCode());

        return employeeRepository.save(employee).toResponse();
    }

    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Removendo funcionário ID: {}", id);
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("Funcionário não encontrado");
        }
        employeeRepository.deleteById(id);
    }
}