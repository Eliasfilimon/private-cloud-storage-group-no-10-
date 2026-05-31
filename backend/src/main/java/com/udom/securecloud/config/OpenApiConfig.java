package com.udom.securecloud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secureCloudStorageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secure Cloud Storage API")
                        .description("REST API for secure file storage, sharing, and management")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("UDOM IT Support")
                                .email("support@udom.ac.tz")
                                .url("https://udom.ac.tz"))
                        .license(new License()
                                .name("Academic License")
                                .url("https://udom.ac.tz")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}
