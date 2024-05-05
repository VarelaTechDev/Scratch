package com.example.repository;

import com.example.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {
    List<Credentials> findByAppUserId(Long appUserId);
    Optional<Credentials> findByAppUserIdAndId(Long appUserId, UUID id);

    // Changed method name to avoid clash with CrudRepository's findById
    List<Credentials> findAllById(UUID id);

    // This method is used to update the 'count' field of a 'Credentials' entity in the database.
    // The @Modifying annotation indicates that this query will modify entities in the database.
    // The @Transactional annotation defines the scope of a single database transaction.
    // The @Query annotation specifies the custom SQL query to be executed.
    // The method takes three parameters: 'count' (the new count), 'id' (the ID of the 'Credentials' entity to update),
    // and 'appUserId' (the ID of the associated 'AppUser').
    // The method returns an 'int', which represents the number of rows affected by the query.
    @Modifying
    @Transactional
    @Query("update Credentials c set c.count = ?1 where c.id = ?2 and c.appUser.id = ?3")
    int updateSignatureCount(long count, UUID id, Long appUserId);
}