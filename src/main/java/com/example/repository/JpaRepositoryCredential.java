package com.example.repository;

import com.example.entity.Credentials;
import com.example.entity.User;
import com.example.utils.ByteArrayAttributeConverter;
import com.example.utils.BytesUtil;
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

    @Transactional
    public void addCredential(long userId, byte[] credentialIdBytes, byte[] publicKeyCoseBytes, String transports, long counter) {
        // Find the user by ID
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        User user = userOptional.get();

        // Create a new Credentials instance
        Credentials credentials = new Credentials();
        credentials.setUser(user);  // Set the user, not the user ID
        credentials.setCredentialId(BytesUtil.bytesToByteArray(credentialIdBytes));  // Convert byte[] to ByteArray
        credentials.setPublicKeyCose(BytesUtil.bytesToByteArray(publicKeyCoseBytes));  // Convert byte[] to ByteArray
        credentials.setTransports(transports);
        credentials.setCount(counter);

        // Save the credentials
        credentialsRepository.save(credentials);
    }
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return new HashSet<>();
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
