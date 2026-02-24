package com.gestao.funcionarios.models.address.entity;

import com.gestao.funcionarios.models.address.dto.AddressResponse;
import com.gestao.funcionarios.models.employee.entity.Employee;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "employee")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "complement")
    private String complement;

    @Column(name = "neighborhood", nullable = false)
    private String neighborhood;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false, length = 2)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 9)
    private String zipCode;

    @Column(name = "country", nullable = false, length = 50)
    private String country = "Brasil";

    @OneToOne(mappedBy = "address", fetch = FetchType.LAZY)
    private Employee employee;

    public AddressResponse toResponse() {
        return new AddressResponse(
                this.street,
                this.number,
                this.neighborhood,
                this.city,
                this.state,
                this.zipCode,
                this.country);
    }
}