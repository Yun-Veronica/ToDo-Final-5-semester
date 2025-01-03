package org.example.todo.controller;

import org.example.todo.model.EmailDetails;
import org.example.todo.model.Users;
import org.example.todo.service.EmailService;
import org.example.todo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class EmailController {

    @Value("${spring.mail.from:default@example.com}") // Значение по умолчанию
    private String msgFrom;

    private final EmailService emailService;
    private final UserService userService;

    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    // Отображение формы для отправки писем
    @GetMapping("/sendMail")
    public String showEmailForm(Model model) {
        List<Users> users = userService.allUsers(); // Получение всех пользователей
        model.addAttribute("users", users); // Добавление списка пользователей в модель
        return "mail/send_mail"; // Рендер страницы с формой
    }

    // Обработка отправки писем
    @PostMapping("/sendMail")
    public String sendMail(@RequestParam("recipientEmail") String recipientEmail,
                           @RequestParam("subject") String subject,
                           @RequestParam("message") String message,
                           Model model) {

        // Создание объекта EmailDetails с данными формы
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(recipientEmail);
        emailDetails.setSubject(subject);
        emailDetails.setMessage(message);
        emailDetails.setFrom(msgFrom);

        // Отправка письма через EmailService
        String status = emailService.sendSimpleMail(emailDetails);

        // Добавление статуса в модель
        model.addAttribute("status", status);
        return "mail/send_mail"; // Возвращение на форму с результатом
    }
}
