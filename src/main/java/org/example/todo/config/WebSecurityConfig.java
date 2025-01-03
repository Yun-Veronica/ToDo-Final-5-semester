package org.example.todo.config;

import org.example.todo.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/registration", "/login", "/user/login", "/static/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
//                        .requestMatchers("/tasks/**").authenticated() // Allow authenticated users to access /tasks/*
//                        .anyRequest().authenticated() // All other requests require authentication
//                )
//                .formLogin(form -> form
//                        .loginPage("/user/login")  // Specify your custom login page
//                        .loginProcessingUrl("/login")  // This is the default login URL for Spring Security
//                        .defaultSuccessUrl("/tasks/all", true)  // Redirect after successful login
//                        .failureUrl("/user/login?error=true")  // Redirect on login failure
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")  // Configure logout URL
//                        .logoutSuccessUrl("/user/login?logout")  // Redirect after successful logout
//                        .permitAll()
//                )
//                .csrf(csrf -> csrf.disable()); // Disable CSRF protection (only if necessary)
//
//        return http.build();
//    }
        http
                    .authorizeRequests()
                    .requestMatchers("/registration","/login", "/user/login", "/static/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                    .requestMatchers("/tasks/**").authenticated()  // Allow authenticated users to access /tasks/*
                    .anyRequest().authenticated() // All other requests require authentication
                .and()
                .formLogin()
                    .loginPage("/user/login")  // Specify your custom login page
                    .loginProcessingUrl("/login")  // This is the default login URL for Spring Security
                    .defaultSuccessUrl("/tasks/all", true)  // Redirect after successful login
                    .failureUrl("/user/login?error=true")  // Redirect on login failure
                    .permitAll()  // Allow all users to access the login page
                .and()
                    .logout()
                    .logoutUrl("/logout")  // Configure logout URL
                    .logoutSuccessUrl("/user/login?logout")  // Redirect after successful logout
                    .permitAll()
                .and()
                    .csrf().disable();  // Disable CSRF protection (only if necessary)

        return http.build();
    }

    // AuthenticationManager bean configuration
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserService userService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userService) // Use your custom UserService for user authentication
                .passwordEncoder(passwordEncoder()); // Set the password encoder for BCrypt password hashing
        return authenticationManagerBuilder.build();
    }
}
