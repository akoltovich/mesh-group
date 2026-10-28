package ru.intro.meshgroup.service;

public interface EmailService {

    void addEmail(Long userId, String email);

    void updateEmail(Long userId, String oldEmail, String newEmail);

    void deleteEmail(Long userId, String email);
}
