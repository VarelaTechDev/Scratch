package org.example.config;


import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "a")
@Component
@Validated
public class AppProperties {

    @NotEmpty
    private String relyingPartyId;

    @NotEmpty
    private String relyingPartyName;

    @NotEmpty
    private Set<String> relyingPartyOrigins;

    public String getRelyingPartyId() {
        return this.relyingPartyId;
    }

    public void setRelyingPartyId(String relyingPartyId) {
        this.relyingPartyId = relyingPartyId;
    }

    public String getRelyingPartyName() {
        return this.relyingPartyName;
    }

    public void setRelyingPartyName(String relyingPartyName) {
        this.relyingPartyName = relyingPartyName;
    }

    public Set<String> getRelyingPartyOrigins() {
        return this.relyingPartyOrigins;
    }

    public void setRelyingPartyOrigins(Set<String> relyingPartyOrigins) {
        this.relyingPartyOrigins = relyingPartyOrigins;
    }
}
