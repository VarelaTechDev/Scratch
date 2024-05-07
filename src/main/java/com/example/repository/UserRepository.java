package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Find a user by username
    Optional<User> findByUserId(Long userId);       // Find a user by user ID

    @Query("SELECT u FROM User u WHERE u.username LIKE :pattern")
    List<User> findByUsernamePattern(@Param("pattern") String pattern);
}

