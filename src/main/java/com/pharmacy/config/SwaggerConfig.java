package com.pharmacy.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter your JWT token. Get it from POST /api/auth/login"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI pharmacyOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Pharmacy Management System API")
                .description("""
                    REST API for Pharmacy Management System.
                    
                    ## Authentication
                    1. Register via `POST /api/auth/register`
                    2. Login via `POST /api/auth/login` to get a JWT token
                    3. Click **Authorize** button above and enter: `<your_token>`
                    4. All protected endpoints will now work
                    
                    ## Features
                    - Medicine Inventory Management
                    - Billing & Order History
                    - Stock Adjustment Audit Trail
                    - Direct `imageUrl`-based medicine images
                    - Soft delete, restore, barcode, and expiring-soon lookups
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Pharmacy Dev Team")
                    .email("dev@pharmacy.com"))
                .license(new License()
                    .name("MIT License")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
