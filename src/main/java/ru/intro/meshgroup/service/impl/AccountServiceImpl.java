package ru.intro.meshgroup.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.intro.meshgroup.dao.repository.AccountRepository;
import ru.intro.meshgroup.exception.InsufficientFundsException;
import ru.intro.meshgroup.service.AccountService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount) {

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Нельзя перевести деньги самому себе.");
        }

        BigDecimal roundedAmount = amount.setScale(2, RoundingMode.HALF_EVEN);

        Long firstLock = Math.min(fromUserId, toUserId);
        Long secondLock = Math.max(fromUserId, toUserId);

        accountRepository.getBalanceForUpdate(firstLock);
        accountRepository.getBalanceForUpdate(secondLock);

        BigDecimal fromBalance = accountRepository.getBalanceForUpdate(fromUserId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт отправителя не найден."));

        BigDecimal toBalance = accountRepository.getBalanceForUpdate(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт получателя не найден."));

        if (fromBalance.compareTo(roundedAmount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств для совершения перевода.");
        }

        BigDecimal newFromBalance = fromBalance.subtract(roundedAmount);
        BigDecimal newToBalance = toBalance.add(roundedAmount);

        accountRepository.updateBalance(fromUserId, newFromBalance);
        accountRepository.updateBalance(toUserId, newToBalance);
    }
}
