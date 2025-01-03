package org.example.todo.controller;

import org.example.todo.model.Users;
import org.example.todo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;

import java.io.File;
import java.util.UUID;


//@Controller
//@RequestMapping("/registration")
//public class RegistrationController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Value("${spring.servlet.multipart.location}")
//    private String uploadDir; // Directory to store uploaded profile pictures
//
//    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB limit for profile picture
//
//    // Show the registration page
//    @GetMapping
//    public String showRegistrationForm(Model model) {
//        model.addAttribute("userForm", new Users());
//        return "user/registration";
//    }
//
//    // Process the registration form
//    @PostMapping
//    public String registerUser(
//            @ModelAttribute("userForm") @Valid Users userForm,
//            BindingResult bindingResult,
//            @RequestParam("profilePicture") MultipartFile profilePicture,
//            Model model) {
//
//        // Validation errors
//        if (bindingResult.hasErrors()) {
//            return "user/registration";
//        }
//
//        // Check if passwords match
//        if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
//            model.addAttribute("passwordError", "Пароли не совпадают");
//            return "user/registration";
//        }
//
//        // Check if user already exists
//        if (userService.findByUsername(userForm.getUsername()) != null) {
//            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
//            return "user/registration";
//        }
//
//        // Handle profile picture upload
//        if (!profilePicture.isEmpty()) {
//            String imagePath = handleProfilePictureUpload(profilePicture, model);
//            if (imagePath == null) {
//                // Error occurred during upload
//                return "user/registration";
//            }
//            userForm.setProfilePicturePath(imagePath);
//        }
//
//        // Encrypt password
//       // userForm.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
//
//        // Save the user
//        userService.saveUser(userForm);
//
//        return "redirect:/tasks/all";
//    }
//
//
//     // Handles the file upload logic for the profile picture.
//
//    private String handleProfilePictureUpload(MultipartFile profilePicture, Model model) {
//        try {
//            // Check file size
//            if (profilePicture.getSize() > MAX_FILE_SIZE) {
//                model.addAttribute("uploadError", "Файл слишком большой. Максимальный размер 10MB.");
//                return null;
//            }
//
//            // Generate a unique file name
//            String fileName = UUID.randomUUID().toString() + "-" + profilePicture.getOriginalFilename();
//
//            // Define absolute path and save the file
//            File destinationFile = new File(uploadDir + File.separator + fileName);
//            profilePicture.transferTo(destinationFile);
//
//            // Return the relative path for database storage
//            return "/images/" + fileName;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            model.addAttribute("uploadError", "Ошибка при загрузке изображения профиля.");
//            return null;
//        }
//    }

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;  // Directory to store uploaded pictures

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", new Users());
        return "user/registration";
    }

    @PostMapping("/registration")
    public String addUser(
            @ModelAttribute("userForm") @Valid Users userForm,
            BindingResult bindingResult,
            @RequestParam("profilePicture") MultipartFile profilePicture, // Receive the file directly
            Model model) {
        // If there are validation errors, return to the form
        if (bindingResult.hasErrors()) {
            return "user/registration";
        }
        // Passwords don't match
        if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            model.addAttribute("passwordError", "Пароли не совпадают");
            return "user/registration";
        }
        // Save the user if the username is unique
        if (!userService.saveUser(userForm)) {
            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
            return "user/registration";
        }
//         Проверка размера файла (10MB)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (!profilePicture.isEmpty() && profilePicture.getSize() > maxFileSize) {
            model.addAttribute("uploadError", "Файл слишком большой. Максимальный размер 10MB.");
            return "user/registration";
        }

        // Логика загрузки фото профиля
        if (!profilePicture.isEmpty()) {
            try {
                // Генерация уникального имени файла
                String fileName = UUID.randomUUID().toString() + "-" + profilePicture.getOriginalFilename();

                // Полный путь к файлу
                String absolutePath = uploadDir; // Используем каталог для загрузки файлов
                File destinationFile = new File(absolutePath + fileName);

                // Сохранение файла
                profilePicture.transferTo(destinationFile);

                // Установка относительного пути в базе данных
                userForm.setProfilePicturePath("/images/" + fileName);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("uploadError", "Ошибка при загрузке изображения профиля!");
                return "user/registration";
            }
        }

        // Save the user
        userService.saveUser(userForm);
        // After registration, redirect to home (or login)
        return "redirect:/user/login";
    }
}