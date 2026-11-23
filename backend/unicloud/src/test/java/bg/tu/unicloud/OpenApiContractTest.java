package bg.tu.unicloud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UC-32 – OpenAPI contract test.
 *
 * <p>Verifies that:
 * <ol>
 *   <li>{@code GET /v3/api-docs} is publicly accessible (no token required) and returns
 *       a valid OpenAPI 3 JSON document.</li>
 *   <li>All major API path prefixes are present in the generated specification so that
 *       SE 2 can generate a client without manual additions.</li>
 *   <li>{@code GET /swagger-ui.html} redirects successfully (Swagger UI is enabled).</li>
 * </ol>
 * </p>
 *
 * <p>The test uses an H2 in-memory database via the {@code test} Spring profile,
 * bypassing the need for a live PostgreSQL instance in CI.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void apiDocsContainsOpenApiVersion() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void apiDocsContainsCoursesPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/courses']").exists());
    }

    @Test
    void apiDocsContainsSearchPath() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/search']").exists());
    }

    @Test
    void apiDocsContainsAuthPath() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/token']").exists());
    }

    @Test
    void apiDocsContainsIntegrationsPath() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/integrations/assignments']").exists());
    }

    @Test
    void apiDocsContainsBearerAuthSecurityScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }

    @Test
    void swaggerUiIsAccessible() throws Exception {
        // Swagger UI redirect – 302 to swagger-ui/index.html is the expected behaviour
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
