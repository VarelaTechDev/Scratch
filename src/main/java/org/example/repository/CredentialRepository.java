package org.example.repository;

import org.example.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, byte[]> {
    List<Credential> findByAppUserId(Long appUserId);
}

