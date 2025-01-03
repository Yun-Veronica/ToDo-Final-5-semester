package org.example.todo.service;

import org.example.todo.model.*;
import org.example.todo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

@Service
public class UserService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfilePictureService profilePictureService;

    @Autowired
    private CategoriesRepository categoryRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder; // This should now be automatically injected

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public Users findUserById(Long userId) {
        Optional<Users> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(null); // Return null if user not found
    }

    public List<Users> allUsers() {
        return userRepository.findAll();
    }


    public Users findByUsername(String username) {
        return userRepository.findByUsername(username); // Fetch the user by their username
    }

    @Transactional
    public boolean saveUser(Users user) {
        Users userFromDB = userRepository.findByUsername(user.getUsername());

        if (userFromDB != null) {
            return false; // User already exists
        }

        Role userRole = roleRepository.findRoleByName("ROLE_USER"); // A method to fetch or create the role
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setId(1L);
            roleRepository.save(userRole);
        }

        user.setRoles(Collections.singleton(userRole));


        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword())); // Use BCrypt for password encryption
        userRepository.save(user);

        return true;
    }

    public boolean deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public List<Users> usergtList(Long idMin) {
        return em.createQuery("SELECT u FROM Users u WHERE u.id > :paramId", Users.class)
                .setParameter("paramId", idMin).getResultList();
    }

    //new for updating profile picture
    @Transactional
    public String updateProfilePicture(Long userId, MultipartFile file) throws IOException {
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setProfilePicturePath(user.getProfilePicturePath());
        userRepository.save(user);

        return "ok";

    }
}
