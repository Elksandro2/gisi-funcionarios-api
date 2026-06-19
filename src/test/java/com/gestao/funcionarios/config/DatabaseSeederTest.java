package com.gestao.funcionarios.config;

import com.gestao.funcionarios.models.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.*;

class DatabaseSeederTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Environment environment;

    private DatabaseSeeder databaseSeeder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseSeeder = new DatabaseSeeder(employeeRepository, environment);
    }

    @Test
    void whenProdAndCountGreaterThan300_thenClearAndSeed() throws Exception {
        // Arrange
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(employeeRepository.count()).thenReturn(450L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(employeeRepository).deleteAll();
        verify(employeeRepository, times(125)).save(any());
    }

    @Test
    void whenProdAndCountLessThanOrEqualTo300_thenSkip() throws Exception {
        // Arrange
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(employeeRepository.count()).thenReturn(125L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(employeeRepository, never()).deleteAll();
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void whenNotProdAndCountGreaterThanZero_thenSkip() throws Exception {
        // Arrange
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(employeeRepository.count()).thenReturn(10L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(employeeRepository, never()).deleteAll();
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void whenNotProdAndCountIsZero_thenSeedWithoutDelete() throws Exception {
        // Arrange
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(employeeRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(employeeRepository, never()).deleteAll();
        verify(employeeRepository, times(125)).save(any());
    }
}
