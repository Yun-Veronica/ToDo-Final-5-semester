package org.example.todo.repository;
import org.example.todo.model.Tasks;
import org.example.todo.model.Users;
import org.example.todo.model.Categories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, Long> {
    List<Tasks> findByUser(Users user);

    List<Tasks> findByCategory(Categories category);

    Page<Tasks> findByUser(Users user, Pageable pageable);

    Page<Tasks> findByNameContainingIgnoreCase(String search, Pageable pageable);

    Page<Tasks> findByUserAndNameContainingIgnoreCase(Users user, String search, Pageable pageable);
}
