package com.loanunderwriting.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI applicationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Application Service API")
                        .description("Handles loan applications and publishes " +
                                "events for underwriting analysis")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Loan Underwriting System")))
                .servers(List.of(new Server()
                        .url("http://localhost:8081")
                        .description("Local Development")));
    }
}