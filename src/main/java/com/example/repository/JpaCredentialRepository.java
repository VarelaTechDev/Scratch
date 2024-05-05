package com.example.repository;

import com.example.entities.AppUser;
import com.example.entities.Credentials;
import com.example.util.BytesUtil;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class JpaCredentialRepository implements CredentialRepository {

    private final AppUserRepository appUserRepository;
    private final CredentialsRepository credentialsRepository;

    public JpaCredentialRepository(AppUserRepository appUserRepository, CredentialsRepository credentialsRepository) {
        this.appUserRepository = appUserRepository;
        this.credentialsRepository = credentialsRepository;
    }

    public void addCredential(long userId, byte[] credentialId, byte[] publicKeyCose,
                              String transports, long counter) {
        Credentials credentials = new Credentials();
        credentials.setId(credentialId);
        AppUser appUser = appUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        credentials.setAppUser(appUser);
        credentials.setPublicKeyCose(publicKeyCose);
        credentials.setTransports(transports);
        credentials.setCount(counter);
        credentialsRepository.save(credentials);
    }

//    @Override
//    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
//        AppUser user = appUserRepository.findByUsername(username);
//        Set<PublicKeyCredentialDescriptor> result = new HashSet<>();
//        if (user != null) {
//            Set<Credentials> credentialsSet = user.getCredentials();
//            for (Credentials credentials : credentialsSet) {
//                Set<AuthenticatorTransport> transports = AuthenticatorTransport.parseTransports(credentials.getTransports());
//                PublicKeyCredentialDescriptor descriptor = PublicKeyCredentialDescriptor.builder()
//                        .id(new ByteArray(credentials.getId())).transports(transports).build();
//                result.add(descriptor);
//            }
//        }
//        return result;
//    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        Set<PublicKeyCredentialDescriptor> result = new HashSet<>();
        if (user != null) {
            Set<Credentials> credentialsSet = user.getCredentials();
            for (Credentials credentials : credentialsSet) {
                Set<AuthenticatorTransport> transports = parseTransports(credentials.getTransports());
                PublicKeyCredentialDescriptor descriptor = PublicKeyCredentialDescriptor.builder()
                        .id(new ByteArray(credentials.getId())).transports(transports).build();
                result.add(descriptor);
            }
        }
        return result;
    }

    private Set<AuthenticatorTransport> parseTransports(String transportsString) {
        Set<AuthenticatorTransport> transports = new HashSet<>();
        if (transportsString != null && !transportsString.isEmpty()) {
            String[] transportItems = transportsString.split(",");
            for (String transport : transportItems) {
                try {
                    transports.add(AuthenticatorTransport.valueOf(transport.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // handle the case where the transport is not recognized
                }
            }
        }
        return transports;
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        long id = BytesUtil.bytesToLong(userHandle.getBytes());
        AppUser user = appUserRepository.findById(id).orElse(null);
        return user != null ? Optional.of(user.getUsername()) : Optional.empty();
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user != null) {
            return Optional.of(new ByteArray(BytesUtil.longToBytes(user.getId())));
        }
        return Optional.empty();
    }

//    @Override
//    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
//        Credentials credential = credentialsRepository.findById(new String(credentialId.getBytes())).orElse(null);
//        if (credential != null && credential.getAppUserId() == BytesUtil.bytesToLong(userHandle.getBytes())) {
//            return Optional.of(RegisteredCredential.builder()
//                    .credentialId(new ByteArray(credential.getId()))
//                    .userHandle(userHandle)
//                    .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
//                    .signatureCount(credential.getCount())
//                    .build());
//        }
//        return Optional.empty();
//    }

//    @Override
//    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
//        Set<RegisteredCredential> result = new HashSet<>();
//        // Correct the conversion here by directly using the byte array
//        Optional<Credentials> credentialOpt = credentialsRepository.findById(credentialId.getBytes());
//        credentialOpt.ifPresent(credential -> {
//            result.add(RegisteredCredential.builder()
//                    .credentialId(new ByteArray(credential.getId()))
//                    .userHandle(new ByteArray(BytesUtil.longToBytes(credential.getAppUser().getId())))  // Corrected
//                    .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
//                    .signatureCount(credential.getCount())
//                    .build());
//        });
//        return result;
//    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        // Extracting byte arrays directly from ByteArray
        byte[] credentialIdBytes = credentialId.getBytes();
        long userId = BytesUtil.bytesToLong(userHandle.getBytes());

        // Using a custom query to find credential by ID and user ID
        Optional<Credentials> credentialOpt = credentialsRepository.findByIdAndUserId(credentialIdBytes, userId);

        return credentialOpt.map(credential -> RegisteredCredential.builder()
                .credentialId(new ByteArray(credential.getId()))
                .userHandle(userHandle)
                .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                .signatureCount(credential.getCount())
                .build());
    }

//    @Override
//    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
//        Set<RegisteredCredential> result = new HashSet<>();
//        Credentials credential = credentialsRepository.findById(new String(credentialId.getBytes())).orElse(null);
//        if (credential != null) {
//            result.add(RegisteredCredential.builder()
//                    .credentialId(new ByteArray(credential.getId()))
//                    .userHandle(new ByteArray(BytesUtil.longToBytes(credential.getAppUserId())))
//                    .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
//                    .signatureCount(credential.getCount())
//                    .build());
//        }
//        return result;
//    }

    public boolean updateSignatureCount(AssertionResult result) {
        byte[] credentialId = result.getCredential().getCredentialId().getBytes();
        Optional<Credentials> credentialOpt = credentialsRepository.findById(credentialId);
        if (credentialOpt.isPresent()) {
            Credentials credential = credentialOpt.get();
            if (credential.getAppUser().getId() == BytesUtil.bytesToLong(result.getCredential().getUserHandle().getBytes())) {
                credential.setCount(result.getSignatureCount());
                credentialsRepository.save(credential);
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        Set<RegisteredCredential> result = new HashSet<>();
        // Directly use the byte array provided by ByteArray for the lookup
        List<Credentials> credentialsList = credentialsRepository.findAllById(credentialId.getBytes());

        for (Credentials credential : credentialsList) {
            // Build and add RegisteredCredential objects to the result set
            result.add(RegisteredCredential.builder()
                    .credentialId(new ByteArray(credential.getId()))
                    .userHandle(new ByteArray(BytesUtil.longToBytes(credential.getAppUser().getId())))
                    .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                    .signatureCount(credential.getCount())
                    .build());
        }
        return result;
    }

}
