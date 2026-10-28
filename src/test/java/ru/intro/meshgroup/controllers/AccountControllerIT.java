package ru.intro.meshgroup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.intro.meshgroup.controllers.request.TransferRequest;
import ru.intro.meshgroup.security.JwtProvider;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DROP TABLE IF EXISTS account", new MapSqlParameterSource());
        jdbcTemplate.update("CREATE TABLE account (user_id BIGINT PRIMARY KEY, balance NUMERIC(19, 2))",
                new MapSqlParameterSource());

        jdbcTemplate.update("INSERT INTO account (user_id, balance) VALUES (1, 1000.00)", new MapSqlParameterSource());
        jdbcTemplate.update("INSERT INTO account (user_id, balance) VALUES (2, 200.00)", new MapSqlParameterSource());
    }

    @Test
    @DisplayName("Интеграционный тест: Успешный трансфер денег через MockMvc и реальный Postgres")
    void shouldPerformTransferSuccessfully() throws Exception {
        // Given
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal transferAmount = new BigDecimal("300.00");

        TransferRequest requestDto = new TransferRequest();
        requestDto.setToUserId(toUserId);
        requestDto.setAmount(transferAmount);

        String token = jwtProvider.generateToken(fromUserId);

        // When & Then
        mockMvc.perform(post("/api/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Трансфер успешно выполнен."));

        BigDecimal actualFromBalance = getBalanceFromDb(fromUserId);
        BigDecimal actualToBalance = getBalanceFromDb(toUserId);

        assertEquals(0, new BigDecimal("700.00").compareTo(actualFromBalance));
        assertEquals(0, new BigDecimal("500.00").compareTo(actualToBalance));
    }

    @Test
    @DisplayName("Интеграционный тест: Ошибка 400 при недостаточном балансе")
    void shouldReturnBadRequestWhenInsufficientFunds() throws Exception {
        // Given
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal hugeAmount = new BigDecimal("5000.00");

        TransferRequest requestDto = new TransferRequest();
        requestDto.setToUserId(toUserId);
        requestDto.setAmount(hugeAmount);

        String token = jwtProvider.generateToken(fromUserId);

        // When & Then
        mockMvc.perform(post("/api/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        assertEquals(0, new BigDecimal("1000.00").compareTo(getBalanceFromDb(fromUserId)));
        assertEquals(0, new BigDecimal("200.00").compareTo(getBalanceFromDb(toUserId)));
    }

    private BigDecimal getBalanceFromDb(Long userId) {
        String sql = "SELECT balance FROM account WHERE user_id = :userId";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("userId", userId), BigDecimal.class);
    }
}