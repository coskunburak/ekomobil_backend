package com.ekomobil.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI ekomobilOpenAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("Ekomobil API")
                        .version("1.0.0")
                        .description("Ekomobil servisleri için REST API dokümantasyonu")
                        .contact(new Contact()
                                .name("Ekomobil Backend")
                                .email("burakkcoskun@hotmail.com")
                        )
                );
    }
}
