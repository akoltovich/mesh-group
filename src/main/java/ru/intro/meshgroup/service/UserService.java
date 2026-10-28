package ru.intro.meshgroup.service;

import ru.intro.meshgroup.controllers.dto.UserDto;
import ru.intro.meshgroup.dao.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface UserService {

    User findUserByLoginAndPassword(String login, String password);

    List<UserDto> searchUsers(
            LocalDate dateOfBirth,
            String phone,
            String name,
            String email,
            int page,
            int size);
}
