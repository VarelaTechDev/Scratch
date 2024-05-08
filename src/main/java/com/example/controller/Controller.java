package com.example.controller;

import com.example.Application;
import com.example.dto.AssertionFinishRequest;
import com.example.dto.AssertionStartResponse;
import com.example.dto.RegistrationFinishRequest;
import com.example.dto.RegistrationStartResponse;
import com.example.entity.Credentials;
import com.example.entity.User;
import com.example.exception.UsernameNotFoundException;
import com.example.repository.JpaRepositoryCredential;
import com.example.repository.UserRepository;
import com.example.utils.BytesUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@Validated
public class Controller {
    private final JpaRepositoryCredential jpaRepositoryCredential;
    @Autowired
    private UserRepository userRepository;

    private final Cache<String, AssertionStartResponse> assertionCache;

    @Autowired
    private RelyingParty relyingParty;

    private final SecureRandom random;

    private final Cache<String, RegistrationStartResponse> registrationCache;


    public Controller(JpaRepositoryCredential jpaRepositoryCredential) {
        this.jpaRepositoryCredential = jpaRepositoryCredential;
        this.random = new SecureRandom();
        this.registrationCache = Caffeine.newBuilder().maximumSize(1000)
                .expireAfterAccess(5, TimeUnit.MINUTES).build();
        this.assertionCache = Caffeine.newBuilder().maximumSize(1000)
                .expireAfterAccess(5, TimeUnit.MINUTES).build();
    }


    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello World";
    }

    // createUser?username=exampleUser
    @PostMapping("/createUser")
    public ResponseEntity<User> createUser (@RequestParam String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists with username: " + username);
        }
        User user = new User();
        user.setUsername(username);
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register/start")
    public RegistrationStartResponse registrationStartResponse
            (@RequestParam(name = "username") String username) {

        // Check if the user exist first
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        PublicKeyCredentialCreationOptions credentialCreation = this.relyingParty
                .startRegistration(StartRegistrationOptions.builder()
                    .user(UserIdentity.builder()
                        .name(user.getUsername())
                        .displayName(user.getUsername())
                        .id(new ByteArray(BytesUtil.longToBytes(user.getUserId())))
                    .build())
                .build());

        byte[] registrationId = new byte[16];
        this.random.nextBytes(registrationId);

        RegistrationStartResponse startResponse = new RegistrationStartResponse(
                Base64.getEncoder().encodeToString(registrationId),
                credentialCreation
        );

        this.registrationCache.put(startResponse.getRegistrationId(), startResponse);

        return startResponse;
    }

    @GetMapping("/printRegistrationCache")
    public Map<String, RegistrationStartResponse> printRegistrationCache() {
        return registrationCache.asMap();
    }

    @PostMapping("/registration/finish")
    public String registrationFinish(@RequestBody RegistrationFinishRequest finishRequest) {

        System.out.println(finishRequest);
        RegistrationStartResponse startResponse = this.registrationCache
                .getIfPresent(finishRequest.getRegistrationId());
        this.registrationCache.invalidate(finishRequest.getRegistrationId());

        try {
            RegistrationResult registrationResult = this.relyingParty
                    .finishRegistration(FinishRegistrationOptions.builder()
                            .request(startResponse.getPublicKeyCredentialCreationOptions())
                            .response(finishRequest.getCredential()).build());

            UserIdentity userIdentity = startResponse.getPublicKeyCredentialCreationOptions()
                    .getUser();

            long userId = BytesUtil.bytesToLong(userIdentity.getId().getBytes());
            String transports = null;

            Optional<SortedSet<AuthenticatorTransport>> transportOptional = registrationResult
                    .getKeyId().getTransports();

            this.jpaRepositoryCredential.addCredential(
                    userId,
                    registrationResult.getKeyId().getId().getBytes(),
                    registrationResult.getPublicKeyCose().getBytes(),
                    transports,
                    registrationResult.getSignatureCount());

        } catch (Exception e) {
            return e.getMessage();
        }


        return "success";
    }

    @PostMapping("/assertion/start")
    public AssertionStartResponse start(@RequestParam String username) {
        byte[] assertionId = new byte[16];
        this.random.nextBytes(assertionId);

        String assertionIdBase64 = Base64.getEncoder().encodeToString(assertionId);
        AssertionRequest assertionRequest = this.relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(username)
                .build());

        AssertionStartResponse response = new AssertionStartResponse(assertionIdBase64, assertionRequest);

        this.assertionCache.put(response.getAssertionId(), response);
        System.out.println(response);
        return response;
    }

    @PostMapping("/assertion/finish")
    public boolean finish(@RequestBody AssertionFinishRequest finishRequest,
                          HttpServletRequest request, HttpServletResponse response) {

        AssertionStartResponse startResponse = this.assertionCache
                .getIfPresent(finishRequest.getAssertionId());
        this.assertionCache.invalidate(finishRequest.getAssertionId());

        try {
            log.info("BEFORE FINISH ASSERTION");
            log.info("finishRequest: " + finishRequest);
            AssertionResult result = this.relyingParty.finishAssertion(
                    FinishAssertionOptions.builder().request(startResponse.getAssertionRequest())
                            .response(finishRequest.getCredential()).build());
            log.info("AFTER");
            System.out.println(result);
            return true;
        }
        catch (AssertionFailedException e) {
            log.error("Assertion failed", e);
        }

        return false;
    }
}