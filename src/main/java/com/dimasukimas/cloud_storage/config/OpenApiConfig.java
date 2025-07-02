package com.dimasukimas.cloud_storage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/",
            "/sign-up",
            "/sign-in"
    );

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud Storage API")
                        .version("1.0")
                        .description("Cloud Storage API for storing files"))
                .components(new Components()
                        .addSecuritySchemes("sessionAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("SESSION")
                        )
                );
    }

    @Bean
    public OpenApiCustomizer customOpenApiResponses() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperations().forEach(operation -> {
                if (PUBLIC_PATHS.contains(path)){
                    operation.setSecurity(Collections.emptyList());
                } else {
                    operation.addSecurityItem(new SecurityRequirement().addList("sessionAuth"));
                }
                    operation.getResponses().addApiResponse("500",
                            new ApiResponse().description("Internal server error"));
            });
        });
    }


}
