package com.example.controller;

import com.yubico.webauthn.RelyingParty;

import java.security.SecureRandom;

@RestController
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;

    private final Cache<String, RegistrationStartResponse> registrationCache;
    private final Cache<String, AssertionStartResponse> assertionCache;

    private final RelyingParty relyingParty;
    private final SecureRandom random;

}
