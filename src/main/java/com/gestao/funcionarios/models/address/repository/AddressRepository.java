package com.gestao.funcionarios.models.address.repository;

import com.gestao.funcionarios.models.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}