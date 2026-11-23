package bg.tu.unicloud.controller;

import bg.tu.unicloud.dto.AuthTokenRequest;
import bg.tu.unicloud.dto.AuthTokenResponse;
import bg.tu.unicloud.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Автентикация и издаване на тестови токени за интеграция")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    @Operation(
            summary = "Издаване на тестов Bearer токен за интеграционни тестове и симулация на SSO профили",
            description = """
                    **Само за разработка / интеграционни тестове!**
                    Издава подписан JWT токен с подадените claims. Всички полета са опционални –
                    при липса се генерират стойности по подразбиране (роля STUDENT, случаен sub).
                    Поставете върнатия token в `Authorization: Bearer <token>` хедъра.
                    """)
    @ApiResponse(responseCode = "200", description = "Токенът е издаден успешно",
            content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
    public ResponseEntity<AuthTokenResponse> issueTestToken(@RequestBody(required = false) AuthTokenRequest request) {
        AuthTokenResponse response = authService.issueToken(request != null ? request : new AuthTokenRequest());
        return ResponseEntity.ok(response);
    }
}
