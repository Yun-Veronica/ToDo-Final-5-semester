package org.example.todo.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@RequiredArgsConstructor
public class Categories {
    @Id
    @Column
    Integer id;

    @Column
    String name;

    @OneToMany(mappedBy = "category")
    private List<Tasks> tasks;

}
