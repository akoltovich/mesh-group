package ru.intro.meshgroup.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.intro.meshgroup.dao.entity.EmailData;
import ru.intro.meshgroup.dao.entity.PhoneData;
import ru.intro.meshgroup.dao.entity.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String FIND_BY_LOGIN_AND_PASSWORD = "SELECT u.id, u.password " +
            "FROM users u " +
            "LEFT JOIN email_data e ON u.id = e.user_id " +
            "LEFT JOIN phone_data p ON u.id = p.user_id " +
            "WHERE (e.email = :login AND u.password = :password) " +
            "   OR (p.phone = :login AND u.password = :password) " +
            "LIMIT 1";

    private static final String SEARCH_QUERY =
            "SELECT u.id as user_id, u.name as user_name, u.date_of_birth, u.password, " +
            "       p.id as phone_id, p.phone, " +
            "       e.id as email_id, e.email " +
            "FROM users u " +
            "LEFT JOIN phone_data p ON u.id = p.user_id " +
            "LEFT JOIN email_data e ON u.id = e.user_id " +
            "WHERE u.id IN (:userIds) " +
            "ORDER BY u.id";

    public Optional<User> findByLoginAndPassword(String login, String password) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", login)
                .addValue("password", password);

        return namedParameterJdbcTemplate.query(FIND_BY_LOGIN_AND_PASSWORD, params, (rs, rowNum) ->
                User.builder()
                        .id(rs.getLong("id"))
                        .password(rs.getString("password"))
                        .build()
        ).stream().findFirst();
    }

    public List<User> searchUsers(
            LocalDate dateOfBirth,
            String phone,
            String name,
            String email,
            int page,
            int size) {

        StringBuilder idQuery = new StringBuilder(
                "SELECT DISTINCT u.id FROM users u " +
                        "LEFT JOIN email_data e ON u.id = e.user_id " +
                        "LEFT JOIN phone_data p ON u.id = p.user_id " +
                        "WHERE 1=1 "
        );

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (dateOfBirth != null) {
            idQuery.append(" AND u.date_of_birth > :dateOfBirth");
            params.addValue("dateOfBirth", dateOfBirth);
        }
        if (phone != null && !phone.isBlank()) {
            idQuery.append(" AND p.phone = :phone");
            params.addValue("phone", phone.trim());
        }
        if (name != null && !name.isBlank()) {
            idQuery.append(" AND LOWER(u.name) LIKE :name");

            params.addValue("name", "%" + name.trim().toLowerCase() + "%");
        }
        if (email != null && !email.isBlank()) {
            idQuery.append(" AND e.email = :email");
            params.addValue("email", email.trim().toLowerCase());
        }

        int offset = page * size;
        idQuery.append(" ORDER BY u.id LIMIT :size OFFSET :offset");
        params.addValue("size", size);
        params.addValue("offset", offset);

        List<Long> userIds = namedParameterJdbcTemplate.queryForList(idQuery.toString(), params, Long.class);

        if (userIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource mainParams = new MapSqlParameterSource("userIds", userIds);

        return namedParameterJdbcTemplate.query(SEARCH_QUERY, mainParams, rs -> {
            Map<Long, User> userMap = new LinkedHashMap<>();

            while (rs.next()) {
                Long userId = rs.getLong("user_id");

                User user = userMap.computeIfAbsent(userId, id -> {
                    try {
                        return User.builder()
                                .id(id)
                                .name(rs.getString("user_name"))
                                .dateOfBirth(rs.getObject("date_of_birth", LocalDate.class))
                                .password(rs.getString("password"))
                                .phones(new ArrayList<>())
                                .emails(new ArrayList<>())
                                .build();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                long phoneId = rs.getLong("phone_id");
                if (!rs.wasNull()) {
                    String phoneValue = rs.getString("phone");
                    boolean alreadyExists = user.getPhones().stream().anyMatch(p -> p.getId() == phoneId);
                    if (!alreadyExists) {
                        user.getPhones().add(new PhoneData(phoneId, userId, phoneValue));
                    }
                }

                long emailId = rs.getLong("email_id");
                if (!rs.wasNull()) {
                    String emailValue = rs.getString("email");
                    boolean alreadyExists = user.getEmails().stream().anyMatch(e -> e.getId() == emailId);
                    if (!alreadyExists) {
                        user.getEmails().add(new EmailData(emailId, userId, emailValue));
                    }
                }
            }
            return new ArrayList<>(userMap.values());
        });
    }
}
