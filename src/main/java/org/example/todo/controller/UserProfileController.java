package org.example.todo.controller;

import org.example.todo.model.Users;
import org.example.todo.repository.UsersRepository;
import org.example.todo.service.ProfilePictureService;
import org.example.todo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;


    @Autowired
    private UserService userService;

    @Autowired
    private ProfilePictureService fileStorageService;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // View profile of logged-in user
    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        Users currentUser = userService.findByUsername(username);

        if (currentUser == null) {
            model.addAttribute("error", "User not found");
            return "error"; // Handle user not found case
        }

        model.addAttribute("user", currentUser);
        return "user/profile"; // The profile view template
    }


    @GetMapping("/edit")
    public String editProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        Users user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login"; // Redirect if user not found
        }
        model.addAttribute("user", user); // Ensure user is added to model
        return "user/edit"; // Thymeleaf template to edit profile
    }


    @PostMapping("/edit")
    public String updateProfile(@RequestParam("profilePicturePath") MultipartFile profileImage,
                                @RequestParam("username") String username,
                                @RequestParam("email") String email,
                                Authentication authentication,
                                Model model) {

        String loggedInUsername = authentication.getName();
        Users currentUser = userService.findByUsername(loggedInUsername);

        if (currentUser != null) {
            // Update user profile
            currentUser.setUsername(username);
            currentUser.setEmail(email);

            // Handle profile image upload
            if (!profileImage.isEmpty()) {
                try {
                    // Define the absolute path where files will be stored (outside static directory)
                    String absolutePath = uploadDir; // The absolute directory path on your server

                    // Generate a unique file name
                    String fileName = UUID.randomUUID().toString() + "-" + profileImage.getOriginalFilename();

                    // Create the destination file using the absolute path
                    File destinationFile = new File(absolutePath + fileName);

                    // Create the directory if it doesn't exist
//                    if (!destinationFile.getParentFile().exists()) {
//                        destinationFile.getParentFile().mkdirs(); // Create parent directory if needed
//                    }

                    // Save the file
                    profileImage.transferTo(destinationFile);

                    // Store only the relative path in the database
                    currentUser.setProfilePicturePath("/images/" + fileName); // Store relative path for URL access

                } catch (IOException e) {
                    model.addAttribute("error", "Error during image upload: " + e.getMessage());
                    return "user/edit"; // Return to edit page if error occurs
                }
            }

            // Save the updated user profile
            userRepository.save(currentUser);
            model.addAttribute("user", currentUser);

            return "redirect:/profile"; // Redirect to profile page after successful update
        } else {
            model.addAttribute("error", "User not found");
            return "user/edit"; // Return to the edit page if user is not found
        }
    }


    @PostMapping("/change-password")
    public String changePassword(Authentication authentication, @RequestParam("newPassword") String newPassword, Model model) {
        String username = authentication.getName();
        Users loggedInUser = userService.findByUsername(username);

        loggedInUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(loggedInUser);
        model.addAttribute("successMessage", "Password updated successfully!");

        return "redirect:/profile"; // Redirect to profile page
    }
}
