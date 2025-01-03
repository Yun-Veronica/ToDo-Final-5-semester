package org.example.todo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.example.todo.model.Users;
import org.example.todo.repository.UsersRepository;

@Service
public class ProfilePictureService {

    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;

    private final UsersRepository usersRepository;

    public ProfilePictureService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }


    public String storeFile(Long userId, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = userId + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfilePicturePath(filePath.toString());
        usersRepository.save(user);

        return filePath.toString();
    }
}
