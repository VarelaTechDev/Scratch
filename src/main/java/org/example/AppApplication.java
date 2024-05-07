package org.example;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.example.config.AppProperties;
import org.example.repository.JpaCredentialRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Bean
    public RelyingParty relyingParty(JpaCredentialRepository jpaCredentialRepository, AppProperties appProperties) {
        RelyingPartyIdentity relyingParty = RelyingPartyIdentity.builder()
                .id(appProperties.getRelyingPartyId())
                .name(appProperties.getRelyingPartyName())
                .build();
        return RelyingParty.builder().identity(relyingParty)
                .credentialRepository(jpaCredentialRepository)
                .origins(appProperties.getRelyingPartyOrigins())
                .build();
    }
}
