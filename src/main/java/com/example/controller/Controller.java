package com.example.controller;

import com.example.dto.RegistrationStartResponse;
import com.example.entity.User;
import com.example.exception.UsernameNotFoundException;
import com.example.repository.UserRepository;
import com.example.utils.BytesUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@Validated
public class Controller {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RelyingParty relyingParty;

    private final SecureRandom random;

    private final Cache<String, RegistrationStartResponse> registrationCache;


    public Controller() {
        this.random = new SecureRandom();
        this.registrationCache = Caffeine.newBuilder().maximumSize(1000)
                .expireAfterAccess(5, TimeUnit.MINUTES).build();
    }


    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello World";
    }

    // createUser?username=exampleUser
    @PostMapping("/createUser")
    public ResponseEntity<User> createUser(@RequestParam String username) {
        User user = new User();
        user.setUsername(username);
        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        log.info("User id: {}", savedUser.getUserId());
        return ResponseEntity.ok(savedUser);
    }

    private static final Marker IMPORTANT = MarkerFactory.getMarker("IMPORTANT");
    private static final Marker NORMAL = MarkerFactory.getMarker("NORMAL");

    @PostMapping("/register/start")
    public RegistrationStartResponse registrationStartResponse
            (@RequestParam(name = "username") String username) {

        // Check if the user exist first
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


        PublicKeyCredentialCreationOptions credentialCreation = this.relyingParty.startRegistration(StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                        .name(username)
                        .displayName(username)
                        .id(new ByteArray(BytesUtil.longToBytes(user.getUserId()))).build())
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .userVerification(UserVerificationRequirement.PREFERRED).build())
                .build());

        byte[] registrationId = new byte[16];
        this.random.nextBytes(registrationId);
        RegistrationStartResponse startResponse = new RegistrationStartResponse(
                Base64.getEncoder().encodeToString(registrationId), credentialCreation);

        this.registrationCache.put(startResponse.getRegistrationId(), startResponse);

        return startResponse;
    }


    @GetMapping("/printRegistrationCache")
    public Map<String, RegistrationStartResponse> printRegistrationCache() {
        return registrationCache.asMap();
    }


//    @GetMapping("/printChallengeCache")
//    public List<Map.Entry<String, String>> printChallengeCache() {
//        challengeCache.asMap().forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
//        return new ArrayList<>(challengeCache.asMap().entrySet());
//    }

//    @GetMapping("/printRegistrationCache")
//    public List<Map.Entry<String, String>> printRegistrationCache() {
//        Map<String, String> stringCache = registrationCache.asMap().entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
//        stringCache.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));
//        return new ArrayList<>(stringCache.entrySet());
//    }

//    public Controller() {
//        this.challengeCache = Caffeine.newBuilder()
//                .expireAfterWrite(10, TimeUnit.MINUTES) // Adjust expiry based on expected registration time
//                .maximumSize(10000) // Adjust based on expected concurrent registrations
//                .build();
//
//        this.registrationCache = Caffeine.newBuilder().maximumSize(1000)
//                .expireAfterAccess(5, TimeUnit.MINUTES).build();
//        this.assertionCache = Caffeine.newBuilder().maximumSize(1000)
//                .expireAfterAccess(5, TimeUnit.MINUTES).build();
//        this.random = new SecureRandom();
//    }

    /*
    1. Generate Registration Parameters

        This involves calling RelyingParty.startRegistration(...) with appropriate options,
        which results in a PublicKeyCredentialCreationOptions object that the front end can use to
        initiate the registration process.
     */
    // /startRegistration?username=exampleUser.

//    @PostMapping("/startRegistration")
//    public RegistrationStartResponse startRegistration(@RequestParam String username) {
//
//        log.info("Start registration for username: {}", username);
//        AppUser user = appUserRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
//        byteBuffer.putLong(user.getId());
//        byteBuffer.flip();  // Prepare buffer to read
//        ByteArray userId = new ByteArray(byteBuffer.array());
//
//        PublicKeyCredentialCreationOptions options = relyingParty
//                .startRegistration(StartRegistrationOptions.builder()
//                        .user(UserIdentity.builder()
//                                .name(user.getUsername())
//                                .displayName(user.getUsername())  // Assuming the display name is the same as the username
//                                .id(userId)
//                                .build())
//                        .build());
////        PublicKeyCredentialCreationOptions options = relyingParty
////            .startRegistration(StartRegistrationOptions.builder()
////                .user(userIdentity)
////                .build());
//
//        byte[] registrationId = new byte[16];
//
//        RegistrationStartResponse startResponse = new RegistrationStartResponse(
//                Base64.getEncoder().encodeToString(registrationId),
//                options
//        );
//
//        // Store the challenge in the cache with username as the key
//        registrationCache.put(startResponse.getRegistrationId(), startResponse);
//
//        log.info("Registration started for username: {}, challenge: {}", username, options.getChallenge().getBase64Url());
//
//        //return options;
//        return startResponse;
//    }

//    private String generateUid() {
//        return UUID.randomUUID().toString();
//    }
//
//    private static final SecureRandom secureRandom = new SecureRandom();


//    private static byte[] generateRandomBytes() {
//        byte[] randomBytes = new byte[16];
//        secureRandom.nextBytes(randomBytes);
//        return randomBytes;
//    }

//    @PostMapping("/startRegistration")
//    public RegistrationStartResponse startRegistration(@RequestParam String username) {
//        AppUser user = appUserRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        UserIdentity userIdentity = UserIdentity.builder()
//                .name(user.getUsername())
//                .displayName(user.getUsername())
//                .id(new ByteArray(Long.toString(user.getId()).getBytes(StandardCharsets.UTF_8))) // Convert user ID to ByteArray
//                .build();
//
//        StartRegistrationOptions startRegOptions = StartRegistrationOptions.builder()
//                .user(userIdentity)
//                .build();
//
//        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(startRegOptions);
//
//        String registrationId = UUID.randomUUID().toString(); // Generate a unique UUID for each session
//
//        RegistrationStartResponse response = new RegistrationStartResponse(
//                registrationId,
//                options
//        );
//
//        registrationCache.put(registrationId, response); // Store using unique ID
//        return response;
//    }

    /*
    2. Finish Registration

        After the user completes the registration process on the client side, the application needs to validate
        and finalize the registration by calling RelyingParty.finishRegistration(...).
     */
    //@PostMapping("/registration/finish")
//    public String registrationFinish(@RequestBody RegistrationFinishRequest finishRequest) {
//        log.info("Finish registration request: {}", finishRequest);
//
//        log.info("Finish registration for registrationId: {}", finishRequest.getRegistrationId());
//        RegistrationStartResponse startResponse = registrationCache.getIfPresent(finishRequest.getRegistrationId());
//        registrationCache.invalidate(finishRequest.getRegistrationId());
//
//        log.info("Checking if StartResponse is null");
//        if (startResponse == null) {
//            log.error("Registration session expired or invalid for registrationId: {}", finishRequest.getRegistrationId());
//            throw new IllegalStateException("Registration session expired or invalid");
//        }
//        log.info("StartResponse is not null");
//
//        log.info("StartResponse.getPublicKeyCredentialCreationOptions: {}", startResponse.getPublicKeyCredentialCreationOptions());
//        log.info("FinishRequest.getCredential: {}", finishRequest.getCredential());
//        try {
//            log.info("Creating registration result");
////            RegistrationResult registrationResult = this.relyingParty
////                    .finishRegistration(FinishRegistrationOptions.builder()
////                            .request(startResponse.getPublicKeyCredentialCreationOptions())
////                            .response(finishRequest.getCredential()).build());
//
//            RegistrationResult registrationResult = this.relyingParty
//                    .finishRegistration(FinishRegistrationOptions.builder()
//                            .request(startResponse.getPublicKeyCredentialCreationOptions())
//                            .response(finishRequest.getCredential())
//                        .build());
//
//            log.info("Built registration result");
//
//            // Example of handling user data and storing credentials
//            log.info("Before UserIdentity");
//            UserIdentity userIdentity = startResponse.getPublicKeyCredentialCreationOptions().getUser();
//            log.info("UserIdentity: {}", userIdentity);
//
//            log.info("Before userid");
//            long userId = BytesUtil.bytesToLong(userIdentity.getId().getBytes());
//            log.info("UserId: {}", userId);
//
//            log.info("Transports");
//            // Handle transport conversion if applicable
//            String transports = registrationResult.getKeyId().getTransports().map(ts -> ts.stream()
//                            .map(AuthenticatorTransport::getId)
//                            .collect(Collectors.joining(",")))
//                    .orElse(null);
//
//            // Store credential data
//
//            jpaRepositoryCredential.addCredential(userId,
//                    registrationResult.getKeyId().getId().getBytes(),
//                    registrationResult.getPublicKeyCose().getBytes(), transports,
//                    registrationResult.getSignatureCount());
//            log.info("Registration successful for registrationId: {}", finishRequest.getRegistrationId());
//            return "Registration successful";
//        } catch (RegistrationFailedException e) {
//            log.error("Registration failed for registrationId: {}", finishRequest.getRegistrationId(), e);
//            throw new RuntimeException("Registration failed", e);
//        }
//    }

//    public static String createDummyAttestationObject() {
//        try {
//            // Create a simple CBOR map with dummy data
//            CBORObject map = CBORObject.NewMap();
//            map.set(CBORObject.FromObject("fmt"), CBORObject.FromObject("none"));
//            map.set(CBORObject.FromObject("attStmt"), CBORObject.NewMap());
//            map.set(CBORObject.FromObject("authData"), CBORObject.FromObject(new byte[37])); // Dummy authenticator data
//
//            // Encode to bytes and then to base64 string
//            byte[] cborBytes = map.EncodeToBytes();
//            return Base64.getEncoder().encodeToString(cborBytes);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


//    @PostMapping("/registration/finish")
//    public String registrationFinish(@RequestBody RegistrationFinishRequest finishRequest) throws IOException, RegistrationFailedException, Base64UrlException {
//
//
//
//        RegistrationStartResponse startResponse = this.registrationCache
//                .getIfPresent(finishRequest.getRegistrationId());
//        this.registrationCache.invalidate(finishRequest.getRegistrationId());
//
//
//        if (startResponse == null) {
//            System.err.println("Start Response is null - Session might have expired or invalid registrationId");
//            return "Session expired or invalid registrationId";
//        }
//
//
//        RegistrationResult registrationResult = this.relyingParty
//                .finishRegistration(FinishRegistrationOptions.builder()
//                        .request(startResponse.getPublicKeyCredentialCreationOptions())
//                        .response(finishRequest.getCredential())
//                        .build());
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