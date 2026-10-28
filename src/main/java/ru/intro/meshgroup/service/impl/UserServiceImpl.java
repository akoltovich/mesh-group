package ru.intro.meshgroup.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.intro.meshgroup.controllers.dto.EmailDataDto;
import ru.intro.meshgroup.controllers.dto.PhoneDataDto;
import ru.intro.meshgroup.controllers.dto.UserDto;
import ru.intro.meshgroup.dao.entity.User;
import ru.intro.meshgroup.dao.repository.UserRepository;
import ru.intro.meshgroup.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(
            value = "user_auth",
            key = "{#login, #password}",
            unless = "#result == null"
    )
    public User findUserByLoginAndPassword(String login, String password) {
        return userRepository.findByLoginAndPassword(login, password).orElse(null);
    }

    @Cacheable(
            value = "user_search",
            key = "{#dateOfBirth, #phone, #name, #email, #page, #size}",
            unless = "#result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(
            LocalDate dateOfBirth, String phone, String name, String email, int page, int size) {

        List<User> users = userRepository.searchUsers(dateOfBirth, phone, name, email, page, size);

        return users.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .dateOfBirth(user.getDateOfBirth())
                        .phones(user.getPhones().stream()
                                .map(p -> new PhoneDataDto(p.getId(), p.getPhone()))
                                .collect(Collectors.toList()))
                        .emails(user.getEmails().stream()
                                .map(e -> new EmailDataDto(e.getId(), e.getEmail()))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
