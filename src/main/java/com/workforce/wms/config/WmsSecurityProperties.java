package com.workforce.wms.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "wms.security")
public record WmsSecurityProperties(
        User admin,
        User employee
) {
    public record User(
            @NotBlank String username,
            @NotBlank String password
    ) {}
}
