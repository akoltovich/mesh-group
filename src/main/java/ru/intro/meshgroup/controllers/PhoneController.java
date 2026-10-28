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
import ru.intro.meshgroup.controllers.request.PhoneRequest;
import ru.intro.meshgroup.service.PhoneService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/phones")
@RequiredArgsConstructor
public class PhoneController {

    private final PhoneService phoneService;

    @PostMapping
    public ResponseEntity<String> addPhone(HttpServletRequest request,
                                           @Valid @RequestBody PhoneRequest phoneRequest) {
        Long userId = (Long) request.getAttribute("USER_ID");
        try {
            phoneService.addPhone(userId, phoneRequest.getPhone());
            return ResponseEntity.ok("Телефон успешно добавлен");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{oldPhone}")
    public ResponseEntity<String> updatePhone(HttpServletRequest request, @PathVariable String oldPhone,
                                              @Valid @RequestBody PhoneRequest phoneRequest) {
        Long userId = (Long) request.getAttribute("USER_ID");
        try {
            phoneService.updatePhone(userId, oldPhone, phoneRequest.getPhone());
            return ResponseEntity.ok("Телефон успешно изменен");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{phone}")
    public ResponseEntity<String> deletePhone(HttpServletRequest request, @PathVariable String phone) {
        Long userId = (Long) request.getAttribute("USER_ID");
        try {
            phoneService.deletePhone(userId, phone);
            return ResponseEntity.ok("Телефон успешно удален");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}