package com.example.repository;

import com.example.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, byte[]> {
    List<Credentials> findByUser_UserId(Long userId); // Find credentials by user ID
    Optional<Credentials> findById(byte[] id); // Find credentials by credential ID
    List<Credentials> findByPublicKeyCose(byte[] publicKeyCose); // Find credentials by public key COSE
    List<Credentials> findByTransportsContaining(String transport);
    List<Credentials> findByCountAndTransportsContaining(Long count, String transport); // Find credentials that include specific transport methods
}

