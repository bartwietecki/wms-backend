package com.workforce.wms.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "wms.security")
public record WmsSecurityProperties(
        User admin,
        @NotEmpty @Valid List<User> employees
) {
    public record User(
            @NotBlank String username,
            @NotBlank String password
    ) {}
}
