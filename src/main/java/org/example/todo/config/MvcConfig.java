package org.example.todo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/user/login").setViewName("user/login");  // Add this to map /user/login to user/login.html
        registry.addViewController("/news").setViewName("news");
    }
}