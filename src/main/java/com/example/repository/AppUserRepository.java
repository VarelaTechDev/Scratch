package com.example.repository;

import com.example.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);

    @Query("SELECT u FROM AppUser u WHERE u.id = :id")
    Optional<AppUser> findById(@Param("id") Long id);
}
