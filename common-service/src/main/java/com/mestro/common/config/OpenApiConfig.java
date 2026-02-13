package com.mestro.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(OpenAPI.class)
public class OpenApiConfig {

    @Value("${openapi.title:API Service}")
    private String title;

    @Value("${openapi.description:RESTful API}")
    private String description;

    @Value("${openapi.version:1.0.0}")
    private String version;

    @Value("${openapi.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${openapi.server-description:Local Development Server}")
    private String serverDescription;

    @Value("${openapi.contact-name:Mestro Support}")
    private String contactName;

    @Value("${openapi.contact-email:support@mestro.com}")
    private String contactEmail;

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI serviceOpenAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);
        server.setDescription(serverDescription);

        Contact contact = new Contact();
        contact.setName(contactName);
        contact.setEmail(contactEmail);

        License license = new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title(title)
                .version(version)
                .description(description)
                .contact(contact)
                .license(license);

        return new OpenAPI().info(info).servers(List.of(server));
    }
}
