package ru.intro.meshgroup.controllers.request;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class EmailRequest {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат адреса электронной почты")
    @Size(max = 200, message = "Email не должен превышать 200 символов")
    private String email;
}
