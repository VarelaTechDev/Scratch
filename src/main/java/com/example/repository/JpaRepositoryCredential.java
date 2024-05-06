package com.example.repository;

import com.example.entity.AppUser;
import com.example.entity.Credentials;
import com.example.exception.UsernameNotFoundException;
import com.example.repository.AppUserRepository;
import com.example.repository.CredentialsRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.RegisteredCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaRepositoryCredential implements CredentialRepository {

    private final AppUserRepository appUserRepository;
    private final CredentialsRepository credentialsRepository;

    public void addCredential(long userId, byte[] credentialId, byte[] publicKeyCose, String transports, long counter) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + userId));

        Credentials credentials = new Credentials();
        credentials.setId(UUID.nameUUIDFromBytes(credentialId)); // Assuming credential ID is unique and can be used as UUID
        credentials.setAppUser(user);
        credentials.setPublicKeyCose(publicKeyCose);
        credentials.setTransports(transports);
        credentials.setCount(counter);

        credentialsRepository.save(credentials);
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> credentialsRepository.findByAppUserId(user.getId())
                        .stream()
                        .map(cred -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(cred.getId().toString().getBytes()))
                                .build())
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> new ByteArray(user.getId().toString().getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        try {
            long userId = Long.parseLong(new String(userHandle.getBytes()));
            return appUserRepository.findById(userId)
                    .map(AppUser::getUsername);
        } catch (NumberFormatException e) {
            // Log the error, handle it, or return an empty Optional based on your error handling policy
            return Optional.empty();
        }
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentialsRepository.findByAppUserIdAndId(Long.parseLong(new String(userHandle.getBytes())), UUID.fromString(credentialId.getBase64()))
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(new ByteArray(cred.getPublicKeyCose()))
                        .signatureCount(cred.getCount())
                        .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentialsRepository.findAllById(UUID.fromString(credentialId.getBase64()))
                .stream()
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(new ByteArray(cred.getId().toString().getBytes()))
                        .userHandle(new ByteArray(cred.getAppUser().getId().toString().getBytes()))
                        .publicKeyCose(new ByteArray(cred.getPublicKeyCose()))
                        .signatureCount(cred.getCount())
                        .build())
                .collect(Collectors.toSet());
    }

}
