package ru.intro.meshgroup.service;

public interface PhoneService {

    void addPhone(Long userId, String phone);

    void updatePhone(Long userId, String oldPhone, String newPhone);

    void deletePhone(Long userId, String phone);
}
