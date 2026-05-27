package com.workforce.wms.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties("wms.cors")
public record WmsCorsProperties(
        @NotEmpty List<@NotBlank String> allowedOrigins
) {}
