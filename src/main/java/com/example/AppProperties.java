package com.example;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private String relyingPartyId;
    private String relyingPartyName;
    private Set<String> relyingPartyOrigins;
}
