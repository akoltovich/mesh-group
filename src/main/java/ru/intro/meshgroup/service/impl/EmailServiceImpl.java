package ru.intro.meshgroup.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.intro.meshgroup.dao.repository.EmailDataRepository;
import ru.intro.meshgroup.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailDataRepository emailDataRepository;

    @Transactional
    public void addEmail(Long userId, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        emailDataRepository.addEmail(userId, email);
    }

    @Transactional
    public void updateEmail(Long userId, String oldEmail, String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Новый Email не может быть пустым");
        }
        boolean updated = emailDataRepository.updateEmail(userId, oldEmail, newEmail);
        if (!updated) {
            throw new IllegalArgumentException(
                    "Не удалось изменить email. Причина: старый адрес не найден, " +
                            "у вас нет прав, либо новый email УЖЕ занят вами или другим пользователем."
            );
        }
    }

    @Transactional
    public void deleteEmail(Long userId, String email) {
        boolean deleted = emailDataRepository.deleteEmailWithCountCheck(userId, email);

        if (!deleted) {
            throw new IllegalArgumentException(
                    "Не удалось удалить email. Причина: запись не найдена, " +
                            "у вас нет прав, либо это ваш единственный (последний) адрес электронной почты."
            );
        }
    }
}
