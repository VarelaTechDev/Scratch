package com.example.repository;

import com.example.entity.Credentials;
import com.example.entity.User;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import lombok.RequiredArgsConstructor;
import org.example.entity.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaRepositoryCredential implements CredentialRepository {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialsRepository credentialsRepository;


    public List<Credentials> getCredentialsByUserId(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return credentialsRepository.findByUser_UserId(userId);
        }
        return List.of(); // Return an empty list if the user does not exist
    }

    @Transactional
    public void addCredential(long userId, byte[] credentialId, byte[] publicKeyCose, String transports, long counter) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        User user = userOptional.get();

        Credentials credentials = new Credentials();
        credentials.setId(credentialId);
        credentials.setUser(user);
        credentials.setPublicKeyCose(publicKeyCose);
        credentials.setTransports(transports);
        credentials.setCount(counter);

        credentialsRepository.save(credentials);
    }

    public byte[] getPublicKeyCose(byte[] credentialId) {
        Optional<Credentials> credentials = credentialsRepository.findById(credentialId);
        return credentials.map(Credentials::getPublicKeyCose).orElse(null);
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return Set.of(); // Return an empty set if the user does not exist
        }

        User user = userOptional.get();
        List<Credentials> credentials = credentialsRepository.findByUser_UserId(user.getUserId());
        return credentials.stream()
                .map(credential -> PublicKeyCredentialDescriptor.builder()
                        .id(new ByteArray(credential.getId()))
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return Optional.empty();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return new HashSet<>();
    }
}
