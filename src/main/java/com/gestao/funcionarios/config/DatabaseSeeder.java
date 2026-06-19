package com.gestao.funcionarios.config;

import com.gestao.funcionarios.models.address.entity.Address;
import com.gestao.funcionarios.models.employee.entity.Employee;
import com.gestao.funcionarios.models.employee.enums.GenderEnum;
import com.gestao.funcionarios.models.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final Random random = new Random();

    public DatabaseSeeder(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando população automática do banco de dados...");

        // Dados base para geração realista
        String[] firstNamesMale = {
                "Gabriel", "Lucas", "Mateus", "João", "Felipe", "Pedro", "Thiago", "Guilherme", "Rafael", "Rodrigo",
                "Gustavo", "Bruno", "André", "Leonardo", "Marcos", "Daniel", "Diego", "Arthur", "Enzo", "Vinicius",
                "Carlos", "Fernando", "Ricardo", "Eduardo", "Alexandre", "Caio", "Vitor", "Marcelo", "Otávio", "Augusto"
        };

        String[] firstNamesFemale = {
                "Maria", "Ana", "Julia", "Beatriz", "Letícia", "Amanda", "Larissa", "Camila", "Mariana", "Gabriela",
                "Isabela", "Carolina", "Luana", "Fernanda", "Juliana", "Bianca", "Rafaela", "Clara", "Sophia", "Laura",
                "Patrícia", "Aline", "Jéssica", "Vanessa", "Bruna", "Camilla", "Leticia", "Renata", "Lívia", "Vitória"
        };

        String[] firstNamesOther = {
                "Alex", "Cris", "Taylor", "Robin", "Val", "Ariel", "Kim", "Morgan", "Sam", "Jordan",
                "Manu", "Sasha", "Denni", "Rene", "Gabi", "Francis", "Charlie", "Joni", "Pati", "Luka"
        };

        String[] lastNames = {
                "Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes",
                "Costa", "Ribeiro", "Martins", "Carvalho", "Teixeira", "Araujo", "Almeida", "Nascimento", "Barbosa", "Rocha",
                "Mendes", "Cardoso", "Freitas", "Pinto", "Dias", "Vieira", "Moreira", "Fernandes", "Machado", "Assis"
        };

        String[] departments = {"Tecnologia", "Recursos Humanos", "Financeiro", "Vendas", "Marketing", "Operações"};

        String[][] rolesPerDepartment = {
                {"Desenvolvedor(a) Software", "Engenheiro(a) de Dados", "Designer UI/UX", "Gerente de Projetos", "Analista de QA", "Product Owner"},
                {"Analista de RH", "Business Partner", "Gerente de DHO", "Recrutador(a)"},
                {"Analista Financeiro", "Contador(a)", "Gerente Financeiro", "Analista de Cobrança"},
                {"Executivo(a) de Contas", "Analista de Vendas", "Gerente Comercial", "SDR"},
                {"Analista de Marketing", "Designer Gráfico", "Especialista em SEO", "Social Media"},
                {"Analista de Operações", "Coordenador(a) de Logística", "Gerente de Operações"}
        };

        String[] streets = {
                "Avenida Paulista", "Rua Augusta", "Rua das Flores", "Avenida Atlântica", "Avenida Brasil", "Rua Bahia",
                "Rua da Consolação", "Avenida Getúlio Vargas", "Rua XV de Novembro", "Rua das Palmeiras", "Avenida Ipiranga",
                "Rua Vergueiro", "Rua Bela Cintra", "Rua Oscar Freire", "Rua Haddock Lobo", "Rua Voluntários da Pátria"
        };

        String[] neighborhoods = {
                "Centro", "Jardins", "Copacabana", "Ipanema", "Vila Mariana", "Pinheiros", "Butantã", "Bela Vista",
                "Itaim Bibi", "Brooklin", "Botafogo", "Flamengo", "Tijuca", "Moema", "Santana", "Barra da Tijuca"
        };

        String[] cities = {
                "São Paulo", "Rio de Janeiro", "Belo Horizonte", "Curitiba", "Porto Alegre",
                "Salvador", "Recife", "Fortaleza", "Brasília", "Campinas"
        };

        String[] states = {"SP", "RJ", "MG", "PR", "RS", "BA", "PE", "CE", "DF", "SP"};

        int totalToGenerate = 150;

        for (int i = 1; i <= totalToGenerate; i++) {
            // Determinar Gênero e Nome
            GenderEnum gender;
            String firstName;
            int genderRoll = random.nextInt(100);
            if (genderRoll < 47) {
                gender = GenderEnum.MASCULINO;
                firstName = firstNamesMale[random.nextInt(firstNamesMale.length)];
            } else if (genderRoll < 94) {
                gender = GenderEnum.FEMININO;
                firstName = firstNamesFemale[random.nextInt(firstNamesFemale.length)];
            } else {
                gender = GenderEnum.OUTRO;
                firstName = firstNamesOther[random.nextInt(firstNamesOther.length)];
            }

            String lastName1 = lastNames[random.nextInt(lastNames.length)];
            String lastName2 = lastNames[random.nextInt(lastNames.length)];
            while (lastName1.equals(lastName2)) {
                lastName2 = lastNames[random.nextInt(lastNames.length)];
            }

            String fullName = firstName + " " + lastName1 + " " + lastName2;
            String email = generateEmail(firstName, lastName1, i);

            // Departamento e Cargo
            int deptIdx = random.nextInt(departments.length);
            String department = departments[deptIdx];
            String[] roles = rolesPerDepartment[deptIdx];
            String role = roles[random.nextInt(roles.length)];

            // Salário proporcional ao cargo e departamento
            double baseSalary = 3000.0 + random.nextDouble() * 11000.0;
            if (role.contains("Gerente") || role.contains("Owner") || role.contains("Partner")) {
                baseSalary += 4000.0;
            }
            BigDecimal salary = BigDecimal.valueOf(baseSalary).setScale(2, RoundingMode.HALF_UP);

            // Data de admissão
            int startYear = 2018;
            int year = startYear + random.nextInt(2026 - startYear);
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28);
            LocalDate admissionDate = LocalDate.of(year, month, day);

            // Endereço
            int cityIdx = random.nextInt(cities.length);
            String city = cities[cityIdx];
            String state = states[cityIdx];

            String street = streets[random.nextInt(streets.length)];
            String number = String.valueOf(1 + random.nextInt(2500));
            String complement = random.nextBoolean() ? null : "Apto " + (10 + random.nextInt(180));
            String neighborhood = neighborhoods[random.nextInt(neighborhoods.length)];
            String zipCode = String.format("%05d-%03d", 10000 + random.nextInt(89999), random.nextInt(1000));

            Address address = Address.builder()
                    .street(street)
                    .number(number)
                    .complement(complement)
                    .neighborhood(neighborhood)
                    .city(city)
                    .state(state)
                    .zipCode(zipCode)
                    .country("Brasil")
                    .build();

            Employee employee = Employee.builder()
                    .name(fullName)
                    .email(email)
                    .gender(gender)
                    .role(role)
                    .department(department)
                    .salary(salary)
                    .admissionDate(admissionDate)
                    .address(address)
                    .build();

            employeeRepository.save(employee);
        }

        log.info("População concluída com sucesso! {} novos funcionários persistidos.", totalToGenerate);
    }

    private String generateEmail(String firstName, String lastName, int id) {
        String cleanFirst = cleanString(firstName);
        String cleanLast = cleanString(lastName);
        return cleanFirst + "." + cleanLast + id + "@gisi.com";
    }

    private String cleanString(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[áàâãä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòôõö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9]", "");
    }
}
