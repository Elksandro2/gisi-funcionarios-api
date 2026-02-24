package com.gestao.funcionarios.models.employee.entity;

import com.gestao.funcionarios.models.address.entity.Address;
import com.gestao.funcionarios.models.employee.dto.EmployeeResponse;
import com.gestao.funcionarios.models.employee.enums.GenderEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderEnum gender;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private BigDecimal salary;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    public EmployeeResponse toResponse() {
        return new EmployeeResponse(
                this.id,
                this.name,
                this.email,
                this.gender,
                this.role,
                this.department,
                this.salary,
                this.admissionDate,
                this.address != null ? this.address.toResponse() : null);
    }
}