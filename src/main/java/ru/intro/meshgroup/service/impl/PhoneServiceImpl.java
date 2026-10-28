package ru.intro.meshgroup.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.intro.meshgroup.dao.repository.PhoneDataRepository;
import ru.intro.meshgroup.service.PhoneService;

@Service
@RequiredArgsConstructor
public class PhoneServiceImpl implements PhoneService {

    private final PhoneDataRepository phoneDataRepository;

    @Transactional
    public void addPhone(Long userId, String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон не может быть пустым");
        }
        phoneDataRepository.addPhone(userId, phone);
    }

    @Transactional
    public void updatePhone(Long userId, String oldPhone, String newPhone) {
        if (newPhone == null || newPhone.isBlank()) {
            throw new IllegalArgumentException("Новый телефон не может быть пустым");
        }
        boolean updated = phoneDataRepository.updatePhone(userId, oldPhone, newPhone);
        if (!updated) {
            throw new IllegalArgumentException(
                    "Не удалось изменить телефон. Причина: старый номер не найден, " +
                            "у вас нет прав, либо новый номер УЖЕ занят в системе."
            );
        }
    }

    @Transactional
    public void deletePhone(Long userId, String phone) {
        boolean deleted = phoneDataRepository.deletePhoneWithCountCheck(userId, phone);

        if (!deleted) {
            throw new IllegalArgumentException(
                    "Не удалось удалить телефон. Причина: запись не найдена, " +
                            "у вас нет прав на её удаление, либо это ваш единственный (последний) номер телефона."
            );
        }
    }
}
