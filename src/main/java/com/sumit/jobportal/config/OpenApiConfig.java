package com.sumit.jobportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // tells Spring: "this class contains bean definitions, read it at startup"
public class OpenApiConfig {

    @Bean   // Spring calls this method and puts the returned OpenAPI object in its context
            // SpringDoc finds this bean automatically and uses it to build the final spec
    public OpenAPI customOpenAPI() {

        // ── PART 1: Security Scheme Definition ──────────────────────────────
        // This teaches Swagger UI that your API uses JWT Bearer tokens.
        // A "SecurityScheme" is just a named definition that says:
        //   "here's HOW authentication works in this API"
        // We give it a name ("bearerAuth") so we can reference it later
        // when marking individual endpoints as "requires auth"

        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)     // transport type = HTTP (not API key, not OAuth)
                .scheme("bearer")                   // HTTP auth scheme = Bearer (as in "Bearer <token>")
                .bearerFormat("JWT")                // hint to UI — the bearer token is a JWT
                .name("bearerAuth");                // internal name we'll reference elsewhere

        // ── PART 2: Global Security Requirement ─────────────────────────────
        // This says: "by default, every endpoint in this API requires bearerAuth"
        // Individual public endpoints (like /register, /login) will override
        // this at the controller level by declaring they need NO security
        // Think of this as: secure by default, opt-out for public endpoints

        SecurityRequirement globalSecurity = new SecurityRequirement()
                .addList("bearerAuth");             // must match the name we gave the scheme above

        // ── PART 3: Contact Info ─────────────────────────────────────────────
        // Optional metadata that appears in the Swagger UI header
        // Useful when you share the docs with others

        Contact contact = new Contact()
                .name("Sumit")
                .email("ry443534@gmail.com");

        // ── PART 4: API Info ─────────────────────────────────────────────────
        // The title, version, and description shown at the top of Swagger UI

        Info info = new Info()
                .title("Job Portal API")
                .version("1.0.0")
                .description("REST API for a job portal — supports user registration, " +
                             "job posting, and application management with JWT authentication.")
                .contact(contact);

        // ── PART 5: Assemble Everything ──────────────────────────────────────
        // OpenAPI is the root object that SpringDoc expects
        // .components() — registers named definitions (our security scheme lives here)
        // .addSecurityItem() — applies the global security requirement to all endpoints
        // .info() — attaches the metadata

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme)) // register the scheme
                .addSecurityItem(globalSecurity)    // apply it globally to all endpoints
                .info(info);                        // attach title/version/description
    }
}