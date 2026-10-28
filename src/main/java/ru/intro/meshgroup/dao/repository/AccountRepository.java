package ru.intro.meshgroup.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String USER_ID = "userId";

    private static final String ACCRUE_INTEREST = "UPDATE account " +
            "SET balance = LEAST((balance * 1.1)::numeric(19, 2), " +
            "(initial_deposit * 2.07)::numeric(19, 2)) " +
            "WHERE balance < initial_deposit * 2.07";

    private static final String GET_BALANCE_FOR_UPDATE =
            "SELECT balance FROM account WHERE user_id = :userId FOR UPDATE";

    private static final String UPDATE_BALANCE = "UPDATE account SET balance = :newBalance WHERE user_id = :userId";

    @Transactional
    public int accrueInterest() {
        return namedParameterJdbcTemplate.update(ACCRUE_INTEREST, new MapSqlParameterSource());
    }

    public Optional<BigDecimal> getBalanceForUpdate(Long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource(USER_ID, userId);
        try {
            BigDecimal balance = namedParameterJdbcTemplate.queryForObject(GET_BALANCE_FOR_UPDATE, params, BigDecimal.class);
            return Optional.ofNullable(balance);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateBalance(Long userId, BigDecimal newBalance) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(USER_ID, userId)
                .addValue("newBalance", newBalance);
        namedParameterJdbcTemplate.update(UPDATE_BALANCE, params);
    }
}
