package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.Data;

@Data
public class RegistrationStartResponse {

  private final String registrationId;

  private final PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

  public RegistrationStartResponse(
          String registrationId,
        PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions) {
    this.registrationId = registrationId;
    this.publicKeyCredentialCreationOptions = publicKeyCredentialCreationOptions;
  }
}