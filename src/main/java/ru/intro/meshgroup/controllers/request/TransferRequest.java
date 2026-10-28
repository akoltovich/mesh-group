package ru.intro.meshgroup.controllers.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    @NotNull(message = "ID получателя обязателен")
    private Long toUserId;

    @NotNull(message = "Сумма перевода обязательна")
    @Positive(message = "Сумма перевода должна быть строго больше нуля")
    private BigDecimal amount;
}