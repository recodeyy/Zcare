package com.pharmacy.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {
    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    @Min(value = 60000, message = "JWT expiration must be at least 1 minute")
    private long expiration;
}
