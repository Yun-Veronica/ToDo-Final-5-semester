package org.example.todo.model;

// Importing required classes

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Annotations
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {

    private String subject;
    private String message;
    private String from;
    private String recipient;

}
