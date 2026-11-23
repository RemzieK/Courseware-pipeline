package bg.tu.unicloud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UC-32 – OpenAPI specification configuration.
 *
 * <p>Registers the global {@code bearerAuth} JWT security scheme and enriches
 * the API {@link Info} block so that SE 2 and other teams receive a complete,
 * self-describing contract from {@code /v3/api-docs} and Swagger UI.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("UniCloud Courseware API")
                        .version("v1")
                        .description("""
                                REST API спецификация – UniCloud Екип 2 (Седмица 1).
                                Покрива курсове, модули, занятия, материали, версии на файлове,
                                търсене с pg_trgm, отметки „прочетено" и интеграции.
                                Договорен контракт за SE 2 (UC-32) и другите екипи (UC-05).
                                Аутентикация: OIDC / SSO; за тестове използвайте POST /api/v1/auth/token.
                                """)
                        .contact(new Contact()
                                .name("Екип 2 – SE 1")
                                .email("team2-se1@tu-sofia.bg"))
                        .license(new License()
                                .name("Internal – TU Sofia")
                                .url("https://tu-sofia.bg")))
                .externalDocs(new ExternalDocumentation()
                        .description("API Contracts (UC-05)")
                        .url("https://github.com/tu-sofia/courseware-pipeline/blob/main/docs/API_CONTRACTS.md"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                OIDC SSO Bearer JWT токен (RS256).
                                                Получете токен от SSO провайдъра или използвайте
                                                POST /api/v1/auth/token за тестови токен.
                                                """)));
    }
}
