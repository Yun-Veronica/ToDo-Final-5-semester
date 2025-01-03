package org.example.todo.controller;

import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ui.Model;

import org.example.todo.model.*;
import org.example.todo.service.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;


@Controller
@RequestMapping("/tasks")
public class TasksController {

    @Autowired
    private TasksService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public String listTasks(Model model,
                            @PageableDefault(size = 10) Pageable pageable,
                            @RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "sortBy", required = false) String sortBy,
                            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction) {

        // Get current authenticated user and check their role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        // Set default sorting
        Sort sort = Sort.by(Sort.Order.asc("creationDate"));

        // Dynamically change sorting if 'sortBy' is provided
        if (sortBy != null && !sortBy.isEmpty()) {
            // Ensure valid direction value (either asc or desc)
            if (direction.equalsIgnoreCase("desc")) {
                sort = Sort.by(Sort.Order.desc(sortBy));
            } else {
                sort = Sort.by(Sort.Order.asc(sortBy));
            }
        }

        // Adjust pageable with the correct sorting order
        Pageable adjustedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Fetch tasks based on role (admin or regular user) and search criteria
        Page<Tasks> tasks;
        Users currentUser = userService.findByUsername(username);

        if (isAdmin) {
            // Admin can view all tasks
            if (search != null && !search.isEmpty()) {
                tasks = taskService.searchTasks(search, adjustedPageable);
            } else {
                tasks = taskService.getAllTasks(adjustedPageable);
            }
        } else {
            // Regular user can only view their own tasks
            if (search != null && !search.isEmpty()) {
                tasks = taskService.searchTasksByUser(currentUser, search, adjustedPageable);
            } else {
                tasks = taskService.getTasksByUser(currentUser, adjustedPageable);
            }
        }

        // Add attributes to the model
        model.addAttribute("tasksPage", tasks);
        model.addAttribute("tasks", tasks.getContent()); // List of tasks for the current page
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "tasks/all";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new Tasks());
        return "tasks/create"; // Thymeleaf template for creating a task
    }

    @PostMapping("/create")
    public String createTask(@ModelAttribute @Valid Tasks task, BindingResult bindingResult, Authentication authentication, Model model) {

        if (bindingResult.hasErrors()) {
            return "tasks/create";  // Return to the create form with validation errors
        }
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) authentication.getPrincipal();
        System.out.println("User:" + user.getId());
        System.out.println("User:" + user.toString());
        if (user == null) {
            model.addAttribute("errorMessage", "Unable to identify the logged-in user.");
            return "tasks/create";
        }

        task.setUser(user); // Set logged-in user

        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            model.addAttribute("task", task);
            model.addAttribute("errorMessage", "Due date cannot be in the past.");
            return "tasks/create";
        }

        try {
            taskService.saveTask(task);
            return "redirect:/tasks/all"; // Redirect to tasks list page
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "tasks/create"; // Return to the task creation form with error
        }
    }


    @GetMapping("/update/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tasks task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        return "tasks/update"; // Thymeleaf template for editing a task
    }


//    @PostMapping("/update/{id}")
//    public String updateTask(@PathVariable Long id, @ModelAttribute @Valid Tasks task, BindingResult bindingResult, Authentication authentication) {
//        if (bindingResult.hasErrors()) {
//            return "tasks/update";  // Return to the edit form with validation errors
//        }
//
//        Tasks existingTask = taskService.getTaskById(id); // Fetch the existing task
//        if (existingTask == null) {
//            throw new IllegalArgumentException("Task not found.");
//        }
//
//        // Preserve the user association
//        Users user = existingTask.getUser();
//        task.setUser(user);
//        task.setId(id); // Ensure task ID is not overwritten
//
//        taskService.saveTask(task);
//        return "redirect:/tasks/all";
//    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute @Valid Tasks task, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "tasks/update";  // Return to the edit form with validation errors
        }

        Tasks existingTask = taskService.getTaskById(id); // Fetch the existing task
        if (existingTask == null) {
            throw new IllegalArgumentException("Task not found.");
        }

        Users user = existingTask.getUser();
        task.setUser(user);
        task.setId(id);

        taskService.saveTask(task);
        return "redirect:/tasks/all?page=0&search=" + (task.getName() != null ? task.getName() : "");
    }


    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks/all";
    }
}

