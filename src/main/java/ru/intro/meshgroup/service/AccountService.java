package ru.intro.meshgroup.service;

import java.math.BigDecimal;

public interface AccountService {

    void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount);
}
