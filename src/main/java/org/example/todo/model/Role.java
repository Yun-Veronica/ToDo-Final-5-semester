package org.example.todo.model;

import java.util.Set;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "t_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {

    @Id
    private Long id;

    private String name;

    @Transient
    @ManyToMany(mappedBy = "roles")
    private Set<Users> users;

    @Override
    public String getAuthority() {
        return getName();
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}