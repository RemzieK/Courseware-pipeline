package bg.tu.unicloud.security;

import bg.tu.unicloud.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserClaimService userClaimService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> claims = jwtTokenService.parseAndValidateToken(token);
            if (claims == null) {
                // Invalid or expired token
                handleAuthenticationFailure(request, response, "Липсва или е невалиден токен.");
                return;
            }

            AuthenticatedUser authenticatedUser = userClaimService.mapAndSynchronizeUser(claims);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    authenticatedUser.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("Authentication failed for request to {}: {}", request.getRequestURI(), e.getMessage());
            // If the failure was due to unsupported role or deactivated account, return 403 Forbidden
            int status = e.getMessage() != null && e.getMessage().contains("роля")
                    ? HttpStatus.FORBIDDEN.value()
                    : HttpStatus.UNAUTHORIZED.value();
            String errorName = status == 403 ? "FORBIDDEN" : "UNAUTHORIZED";

            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(status)
                    .error(errorName)
                    .message(e.getMessage() != null ? e.getMessage() : "Грешка при автентикация.")
                    .path(request.getRequestURI())
                    .timestamp(Instant.now())
                    .build();

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        }
    }

    private void handleAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message(message)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
