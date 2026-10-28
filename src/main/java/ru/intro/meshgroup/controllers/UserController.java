package ru.intro.meshgroup.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.intro.meshgroup.controllers.dto.UserDto;
import ru.intro.meshgroup.service.UserService;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateOfBirth,

            @RequestParam(required = false)
            @Pattern(regexp = "^7\\d{10}$", message = "Телефон должен быть в формате 7XXXXXXXXXX") String phone,

            @RequestParam(required = false) String name,

            @RequestParam(required = false)
            @Email(message = "Некорректный формат email") String email,

            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Номер страницы не может быть отрицательным") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Размер страницы должен быть не менее 1")
            @Max(value = 100, message = "Размер страницы не может превышать 100") int size
    ) {
        List<UserDto> users = userService.searchUsers(dateOfBirth, phone, name, email, page, size);

        return ResponseEntity.ok(users);
    }
}
