package com.example.rent;

import com.example.rent.domain.Customer;
import com.example.rent.domain.Equipment;
import com.example.rent.domain.EquipmentStatus;
import com.example.rent.dto.CreateCustomerRequest;
import com.example.rent.dto.CreateRentalRequest;
import com.example.rent.repository.CustomerRepository;
import com.example.rent.repository.EquipmentRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RentIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("labs")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void createCustomer_ShouldReturnCreated() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("test@mail.ru", "Иван", "+79991234567");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createRental_ShouldReturnConflict_WhenDatesOverlap() throws Exception {
        Customer customer = new Customer();
        customer.setEmail("rent@mail.ru");
        customer.setFullName("Петр");
        customer.setPhone("+79990000000");
        customer = customerRepository.save(customer);

        Equipment equipment = new Equipment();
        equipment.setName("Камера");
        equipment.setCategory("Фото");
        equipment.setDailyPrice(new BigDecimal("100"));
        equipment.setStatus(EquipmentStatus.available);
        equipment = equipmentRepository.save(equipment);

        CreateRentalRequest firstRental = new CreateRentalRequest(
                customer.getId(), equipment.getId(),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5)
        );

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRental)))
                .andExpect(status().isCreated());

        CreateRentalRequest secondRental = new CreateRentalRequest(
                customer.getId(), equipment.getId(),
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7)
        );

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRental)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCustomer_ShouldReturnUnprocessableEntity_WhenEmailInvalid() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("not-an-email", "Иван", "+79991234567");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}