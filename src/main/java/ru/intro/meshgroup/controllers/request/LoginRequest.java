package ru.intro.meshgroup.controllers.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Логин (email или телефон) не может быть пустым")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 50, message = "Пароль должен быть от 6 до 50 символов")
    private String password;
}