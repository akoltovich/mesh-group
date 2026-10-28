package ru.intro.meshgroup.controllers.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class PhoneRequest {

    @NotBlank(message = "Номер телефона не может быть пустым")
    @Pattern(regexp = "^7\\d{10}$", message = "Телефон должен быть в формате 7XXXXXXXXXX (11 цифр, начиная с 7)")
    private String phone;
}
