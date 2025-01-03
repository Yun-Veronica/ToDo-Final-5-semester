package org.example.todo.service;

import org.example.todo.model.EmailDetails;

public interface EmailService {
    String sendSimpleMail(EmailDetails details);
}
