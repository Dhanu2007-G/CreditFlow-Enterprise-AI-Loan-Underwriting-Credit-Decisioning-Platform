package com.loanunderwriting.underwriting.config;

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
    public OpenAPI underwritingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Underwriting Service API")
                        .description("Consumes loan applications, scores risk, " +
                                "and makes underwriting decisions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Loan Underwriting System")))
                .servers(List.of(new Server()
                        .url("http://localhost:8082")
                        .description("Local Development")));
    }
}