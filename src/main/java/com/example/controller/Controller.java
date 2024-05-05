package com.example.controller;

import com.example.dto.AssertionStartResponse;
import com.example.dto.RegistrationStartResponse;
import com.example.entity.AppUser;
import com.example.exception.ResponseExpiredException;
import com.example.exception.UsernameNotFoundException;
import com.example.repository.AppUserRepository;
import com.example.repository.JpaRepositoryCredential;
import com.example.utils.BytesUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Validated
public class Controller {


    private Cache<String, String> challengeCache;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RelyingParty relyingParty;

    @Autowired
    private JpaRepositoryCredential jpaRepositoryCredential;


    @GetMapping("/printChallengeCache")
    public List<Map.Entry<String, String>> printChallengeCache() {
        challengeCache.asMap().forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
        return new ArrayList<>(challengeCache.asMap().entrySet());
    }

    public Controller() {
        this.challengeCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // Adjust expiry based on expected registration time
                .maximumSize(10000) // Adjust based on expected concurrent registrations
                .build();
    }
    // createUser?username=exampleUser
    @PostMapping("/createUser")
    public AppUser createUser(@RequestParam String username) {
        log.info("Creating user with username: {}", username);
        AppUser user = new AppUser();
        user.setUsername(username);
        return appUserRepository.save(user);
    }

    /*
    1. Generate Registration Parameters

        This involves calling RelyingParty.startRegistration(...) with appropriate options,
        which results in a PublicKeyCredentialCreationOptions object that the front end can use to
        initiate the registration process.
     */
    // /startRegistration?username=exampleUser.

    @PostMapping("/startRegistration")
    public PublicKeyCredentialCreationOptions startRegistration(@RequestParam String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(user.getId());
        byteBuffer.flip();  // Prepare buffer to read

        ByteArray userId = new ByteArray(byteBuffer.array());

        UserIdentity userIdentity = UserIdentity.builder()
                .name(user.getUsername())
                .displayName(user.getUsername())
                .id(userId)
                .build();

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(StartRegistrationOptions.builder()
                .user(userIdentity)
                .build());

        // Store the challenge in the cache with username as the key
        challengeCache.put(username, options.getChallenge().getBase64Url());

        return options;
    }


    /*
    2. Finish Registration

        After the user completes the registration process on the client side, the application needs to validate
        and finalize the registration by calling RelyingParty.finishRegistration(...).
     */
//    @PostMapping("/finishRegistration")
//    public RegistrationResult finishRegistration(@RequestBody FinishRegistrationRequest finishRequest) {
//        return null;
//    }


    /*
    3. Start Assertion (Login)

This operation prepares the login request data by calling RelyingParty.startAssertion(...)
which returns an AssertionRequest that the front end uses to initiate the login process.
     */
//    @PostMapping("/startAssertion")
//    public AssertionRequest startAssertion(@RequestParam String username) {
//        AssertionRequestOptions options = AssertionRequestOptions.builder()
//                .username(username)
//                .build();
//
//        AssertionRequest request = relyingParty.startAssertion(options);
//        assertionCache.put(request.getChallenge().getBase64(), request);
//        return request;
//    }
//
//    /*
//    4. Finish Assertion (Login)
//
//After receiving the assertion from the client, your application will validate it using RelyingParty.finishAssertion(...),
// which performs the actual verification of the user's presented credentials.
//     */
//    @PostMapping("/finishAssertion")
//    public AssertionResult finishAssertion(@RequestBody FinishAssertionRequest finishRequest) {
//        AssertionStartResponse startResponse = assertionCache.getIfPresent(finishRequest.getChallenge());
//        if (startResponse == null) {
//            throw new ResponseExpiredException("Assertion start response expired");
//        }
//
//        FinishAssertionOptions options = FinishAssertionOptions.builder()
//                .request(startResponse)
//                .response(finishRequest.getCredential())
//                .build();
//
//        AssertionResult result = relyingParty.finishAssertion(options);
//        // Update the signature counter in the credential repository
//        credentialRepository.updateSignatureCount(result.getSignatureCount(), result.getCredentialId(), result.getUserHandle());
//        return result;
//    }
}