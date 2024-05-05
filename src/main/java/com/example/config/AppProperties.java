package com.example.config;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
// import jakarta.validation.constraints.NotEmpty; <-- Issues
import javax.validation.constraints.NotEmpty;

// @ConfigurationProperties allows you to map your property values to Java class fields.
// The 'prefix' attribute is used to specify the prefix to be used for the names of the properties.
@ConfigurationProperties(prefix = "app")

// @Component is a generic stereotype for any Spring-managed component.
// Spring will automatically pick up classes annotated with @Component for dependency injection.
@Component

// @Validated enables validation of @ConfigurationProperties classes.
// It allows you to use JSR-303 bean validation annotations on your configuration classes.
@Validated
public class AppProperties {

  // @NotEmpty is a bean validation annotation that ensures that the annotated element is not null or empty.
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