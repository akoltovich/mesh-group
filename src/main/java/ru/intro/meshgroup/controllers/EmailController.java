package ru.intro.meshgroup.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.intro.meshgroup.controllers.request.EmailRequest;
import ru.intro.meshgroup.service.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<String> addEmail(HttpServletRequest request, @Valid @RequestBody EmailRequest emailRequest) {
        Long userId = (Long) request.getAttribute("USER_ID");
        try {
            emailService.addEmail(userId, emailRequest.getEmail());
            return ResponseEntity.ok("Email успешно добавлен");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{oldEmail}")
    public ResponseEntity<String> updateEmail(HttpServletRequest request, @PathVariable String oldEmail,
                                              @Valid @RequestBody EmailRequest emailRequest) {
        Long userId = (Long) request.getAttribute("USER_ID");
        try {
            emailService.updateEmail(userId, oldEmail, emailRequest.getEmail());
            return ResponseEntity.ok("Email успешно изменен");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteEmail(HttpServletRequest request, @PathVariable String email) {
        Long userId = (Long) request.getAttribute("USER_ID");
        try {
            emailService.deleteEmail(userId, email);
            return ResponseEntity.ok("Email успешно удален");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}