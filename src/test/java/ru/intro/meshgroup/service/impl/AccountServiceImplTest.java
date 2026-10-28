package ru.intro.meshgroup.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.intro.meshgroup.dao.repository.AccountRepository;
import ru.intro.meshgroup.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldTransferMoneySuccessfully() {
        // Given
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = new BigDecimal("100.125");
        BigDecimal expectedRoundedAmount = new BigDecimal("100.12");

        BigDecimal initialFromBalance = new BigDecimal("1000.00");
        BigDecimal initialToBalance = new BigDecimal("200.00");

        when(accountRepository.getBalanceForUpdate(fromUserId)).thenReturn(Optional.of(initialFromBalance));
        when(accountRepository.getBalanceForUpdate(toUserId)).thenReturn(Optional.of(initialToBalance));

        // When
        assertDoesNotThrow(() -> accountService.transferMoney(fromUserId, toUserId, amount));

        // Then
        BigDecimal expectedNewFromBalance = initialFromBalance.subtract(expectedRoundedAmount);
        BigDecimal expectedNewToBalance = initialToBalance.add(expectedRoundedAmount);

        verify(accountRepository, times(1)).updateBalance(fromUserId, expectedNewFromBalance);
        verify(accountRepository, times(1)).updateBalance(toUserId, expectedNewToBalance);
    }

    @Test
    void shouldThrowExceptionWhenTransferringToSameUser() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.transferMoney(userId, userId, amount)
        );

        assertEquals("Нельзя перевести деньги самому себе.", exception.getMessage());

        verifyNoInteractions(accountRepository);
    }

    @Test
    void shouldThrowExceptionWhenFromUserNotFound() {
        // Given
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        when(accountRepository.getBalanceForUpdate(fromUserId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.transferMoney(fromUserId, toUserId, amount)
        );

        assertEquals("Аккаунт отправителя не найден.", exception.getMessage());
        verify(accountRepository, never()).updateBalance(anyLong(), any());
    }

    @Test
    @DisplayName("Ошибка: Аккаунт получателя не найден")
    void shouldThrowExceptionWhenToUserNotFound() {
        // Given
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        when(accountRepository.getBalanceForUpdate(fromUserId)).thenReturn(Optional.of(new BigDecimal("500.00")));
        when(accountRepository.getBalanceForUpdate(toUserId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.transferMoney(fromUserId, toUserId, amount)
        );

        assertEquals("Аккаунт получателя не найден.", exception.getMessage());
        verify(accountRepository, never()).updateBalance(anyLong(), any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        // Given
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = new BigDecimal("1000.00");

        when(accountRepository.getBalanceForUpdate(fromUserId)).thenReturn(Optional.of(new BigDecimal("500.00")));
        when(accountRepository.getBalanceForUpdate(toUserId)).thenReturn(Optional.of(new BigDecimal("200.00")));

        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () ->
                accountService.transferMoney(fromUserId, toUserId, amount)
        );

        assertEquals("Недостаточно средств для совершения перевода.", exception.getMessage());

        verify(accountRepository, never()).updateBalance(anyLong(), any());
    }
}