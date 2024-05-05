package com.example;

import com.example.config.AppProperties;
import com.example.repository.JpaRepositoryCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RelyingParty relyingParty(JpaRepositoryCredential jpaRepositoryCredential,
                                     AppProperties appProperties) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(appProperties.getRelyingPartyId()).name(appProperties.getRelyingPartyName())
                .build();

        return RelyingParty.builder().identity(rpIdentity)
                .credentialRepository(jpaRepositoryCredential)
                .origins(appProperties.getRelyingPartyOrigins()).build();
    }

}