package com.gestao.funcionarios.models.employee.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gestao.funcionarios.models.employee.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    @Query("SELECT COUNT(e) as totalEmployees, " +
            "COALESCE(SUM(e.salary), 0) as totalSalary, " +
            "COALESCE(AVG(e.salary), 0.0) as averageSalary " +
            "FROM Employee e")
    EmployeeSummary getGlobalSummary();

    @Query("SELECT e.gender as name, COUNT(e) as value FROM Employee e GROUP BY e.gender")
    List<ChartProjection> getGenderDistribution();

    @Query("SELECT e.department as name, COUNT(e) as value FROM Employee e GROUP BY e.department")
    List<ChartProjection> getDeptDistribution();

    @Query("SELECT e.address.city as name, COUNT(e) as value FROM Employee e GROUP BY e.address.city")
    List<ChartProjection> getCityDistribution();

    @Query("SELECT EXTRACT(YEAR FROM e.admissionDate) as name, COUNT(e) as value " +
            "FROM Employee e " +
            "GROUP BY EXTRACT(YEAR FROM e.admissionDate) " +
            "ORDER BY EXTRACT(YEAR FROM e.admissionDate)")
    List<ChartProjection> getAdmissionYearDistribution();

    interface EmployeeSummary {
        long getTotalEmployees();

        BigDecimal getTotalSalary();

        Double getAverageSalary();
    }

    interface ChartProjection {
        String getName();

        Long getValue();
    }
}