package ru.intro.meshgroup.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.intro.meshgroup.controllers.request.TransferRequest;
import ru.intro.meshgroup.exception.InsufficientFundsException;
import ru.intro.meshgroup.service.AccountService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(HttpServletRequest request,
                                           @Valid @RequestBody TransferRequest transferRequest) {

        Long fromUserId = (Long) request.getAttribute("USER_ID");

        if (fromUserId == null) {
            return ResponseEntity.status(401).body("Пользователь не авторизован.");
        }

        try {
            accountService.transferMoney(fromUserId, transferRequest.getToUserId(), transferRequest.getAmount());
            return ResponseEntity.ok("Трансфер успешно выполнен.");
        } catch (IllegalArgumentException | InsufficientFundsException e) {
            return ResponseEntity.badRequest().body("Ошибка при выполнении операции: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при выполнении операции: " + e.getMessage());
        }
    }
}
