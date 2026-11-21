package bg.tu.unicloud.config;

import bg.tu.unicloud.security.CustomOidcUserService;
import bg.tu.unicloud.security.JwtAuthenticationFilter;
import bg.tu.unicloud.security.RestAccessDeniedHandler;
import bg.tu.unicloud.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final RestAuthenticationEntryPoint authenticationEntryPoint;
        private final RestAccessDeniedHandler accessDeniedHandler;
        private final CustomOidcUserService customOidcUserService;
        private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .authorizeHttpRequests(auth -> auth
                                                // Public system endpoints
                                                .requestMatchers(
                                                                "/actuator/health",
                                                                "/actuator/info",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/api/v1/auth/**")
                                                .permitAll()

                                                // Integrations for Service (AutoJudge) and Admin
                                                .requestMatchers("/api/v1/integrations/**")
                                                .hasAnyRole("SERVICE", "ADMIN")

                                                // Public catalog read access
                                                .requestMatchers(HttpMethod.GET, "/api/v1/courses").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/courses/{courseId}")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/lessons/{lessonId}/materials")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/materials/{materialId}")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/materials/{materialId}/versions")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/materials/{materialId}/file")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/search").permitAll()

                                                // Student read marks & progress
                                                .requestMatchers("/api/v1/materials/{materialId}/read")
                                                .hasAnyRole("STUDENT", "ADMIN")
                                                .requestMatchers("/api/v1/courses/{courseId}/progress")
                                                .hasAnyRole("STUDENT", "ADMIN")

                                                // Teacher / Admin mutations
                                                .requestMatchers(HttpMethod.POST, "/api/v1/courses")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/courses/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/modules")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/modules/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/modules/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/modules/*/lessons")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/lessons/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/lessons/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/lessons/*/materials")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/materials/**")
                                                .hasAnyRole("TEACHER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/materials/*/versions")
                                                .hasAnyRole("TEACHER", "ADMIN")

                                                // Everything else requires authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                // OIDC Authorization Code Flow with PKCE support when configured
                clientRegistrationRepository.ifPresent(repo -> {
                        http.oauth2Login(oauth2 -> oauth2
                                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService)));
                });

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of("*")); // TODO: change to specific origin on production
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept",
                                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
                configuration.setExposedHeaders(List.of("Cache-Control", "Content-Disposition", "ETag"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
