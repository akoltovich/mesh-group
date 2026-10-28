package ru.intro.meshgroup.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.intro.meshgroup.exception.EmailAlreadyExistsException;

@Repository
@RequiredArgsConstructor
public class EmailDataRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String ADD_EMAIL = "INSERT INTO email_data (id, user_id, email) " +
            "VALUES (nextval('email_id_seq'), :userId, :email) " +
            "ON CONFLICT (email) DO NOTHING";

    private static final String UPDATE_EMAIL =
            "UPDATE email_data SET email = :newEmail WHERE email = :oldEmail AND user_id = :userId";

    private static final String DELETE_EMAIL = "DELETE FROM email_data " +
            "WHERE email = :email " +
            "  AND user_id = :userId " +
            "  AND (SELECT COUNT(*) FROM email_data WHERE user_id = :userId) > 1";

    private static final String EMAIL = "email";
    private static final String USER_ID = "userId";

    @CacheEvict(value = "user_search", allEntries = true)
    public void addEmail(Long userId, String email) {
        String cleanEmail = (email != null) ? email.trim().toLowerCase() : null;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(USER_ID, userId)
                .addValue(EMAIL, cleanEmail);

        int rowsAffected = namedParameterJdbcTemplate.update(ADD_EMAIL, params);

        if (rowsAffected == 0) {
            throw new EmailAlreadyExistsException("Email '" + cleanEmail + "' уже занят");
        }
    }

    @CacheEvict(value = {"user_search", "user_auth"}, allEntries = true)
    public boolean updateEmail(Long userId, String oldEmail, String newEmail) {
        String cleanNewEmail = (newEmail != null) ? newEmail.trim().toLowerCase() : null;

        if (oldEmail != null && oldEmail.equalsIgnoreCase(newEmail)) {
            throw new EmailAlreadyExistsException("Email '" + newEmail + "' уже зарегистрирован");
        }

        String cleanOldEmail = (oldEmail != null) ? oldEmail.trim().toLowerCase() : null;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("newEmail", cleanNewEmail)
                .addValue("oldEmail", cleanOldEmail)
                .addValue(USER_ID, userId);

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(UPDATE_EMAIL, params);

            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Не удалось обновить email. Неверные данные или доступ запрещен.");
            }

            return true;

        } catch (DuplicateKeyException e) {
            throw new EmailAlreadyExistsException("Email '" + cleanNewEmail + "' уже занят другим аккаунтом");
        }
    }

    @CacheEvict(value = "user_search", allEntries = true)
    public boolean deleteEmailWithCountCheck(Long userId, String email) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EMAIL, email)
                .addValue(USER_ID, userId);

        return namedParameterJdbcTemplate.update(DELETE_EMAIL, params) > 0;
    }
}
