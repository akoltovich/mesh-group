package ru.intro.meshgroup.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.intro.meshgroup.dao.repository.AccountRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSchedulerService {

    private final AccountRepository accountRepository;

    @Scheduled(fixedRate = 30000, initialDelay = 5000)
    public void runInterestAccrual() {
        log.info("Запуск автоматического начисления процентов (10%)...");

        try {
            int updatedRows = accountRepository.accrueInterest();

            if (updatedRows == 0) {
                throw new IllegalStateException("Начисление остановлено: " +
                        "все аккаунты в системе уже достигли максимального лимита в 207% от первоначального депозита.");
            }

            log.info("Проценты успешно начислены для {} аккаунтов.", updatedRows);

        } catch (IllegalStateException e) {
            log.warn("Предупреждение планировщика: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Критическая ошибка при начислении процентов: ", e);
        }
    }
}