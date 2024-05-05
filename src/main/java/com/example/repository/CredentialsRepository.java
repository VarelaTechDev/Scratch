package com.example.repository;

import com.example.entities.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, byte[]> {
    // Method to find a single Credentials by its ID (automatically provided by extending JpaRepository)
    Optional<Credentials> findById(byte[] credentialId);

    // Custom method to find a Credentials by its ID and the associated user's ID
    @Query("SELECT c FROM Credentials c WHERE c.id = :credentialId AND c.appUser.id = :userId")
    Optional<Credentials> findByIdAndUserId(@Param("credentialId") byte[] credentialId, @Param("userId") long userId);

    // Method to find all Credentials with the same ID (potentially used if IDs are not unique, which is unusual)
    List<Credentials> findAllById(byte[] id);
}
