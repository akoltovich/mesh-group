package ru.intro.meshgroup.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.intro.meshgroup.exception.PhoneAlreadyExistsException;

@Repository
@RequiredArgsConstructor
public class PhoneDataRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String ADD_PHONE = "INSERT INTO phone_data (id, user_id, phone) " +
            "VALUES (nextval('phone_id_seq'), :userId, :phone) " +
            "ON CONFLICT (phone) DO NOTHING";

    private static final String UPDATE_PHONE =
            "UPDATE phone_data SET phone = :newPhone WHERE phone = :oldPhone AND user_id = :userId";

    private static final String DELETE_PHONE = "DELETE FROM phone_data " +
            "WHERE phone = :phone " +
            "  AND user_id = :userId " +
            "  AND (SELECT COUNT(*) FROM phone_data WHERE user_id = :userId) > 1";

    private static final String PHONE = "phone";
    private static final String USER_ID = "userId";

    @CacheEvict(value = "user_search", allEntries = true)
    public void addPhone(Long userId, String phone) {
        String cleanPhone = (phone != null) ? phone.trim() : null;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(USER_ID, userId)
                .addValue(PHONE, cleanPhone);

        int rowsAffected = namedParameterJdbcTemplate.update(ADD_PHONE, params);

        if (rowsAffected == 0) {
            throw new PhoneAlreadyExistsException("Телефон '" + cleanPhone + "' уже занят");
        }
    }

    @CacheEvict(value = {"user_search", "user_auth"}, allEntries = true)
    public boolean updatePhone(Long userId, String oldPhone, String newPhone) {
        String cleanNewPhone = (newPhone != null) ? newPhone.trim() : null;

        if (oldPhone != null && oldPhone.equals(newPhone)) {
            throw new PhoneAlreadyExistsException("Телефон '" + newPhone + "' уже зарегистрирован");
        }

        String cleanOldPhone = (oldPhone != null) ? oldPhone.trim() : null;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("newPhone", cleanNewPhone)
                .addValue("oldPhone", cleanOldPhone)
                .addValue(USER_ID, userId);

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(UPDATE_PHONE, params);

            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Не удалось обновить телефон. Неверные данные или доступ запрещен.");
            }

            return true;

        } catch (DuplicateKeyException e) {
            throw new PhoneAlreadyExistsException("Телефон '" + cleanNewPhone + "' уже занят другим аккаунтом");
        }
    }

    @CacheEvict(value = "user_search", allEntries = true)
    public boolean deletePhoneWithCountCheck(Long userId, String phone) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PHONE, phone)
                .addValue(USER_ID, userId);

        return namedParameterJdbcTemplate.update(DELETE_PHONE, params) > 0;
    }
}
