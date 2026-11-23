package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Издаден Bearer JWT токен (само за разработка / интеграционни тестове)")
public class AuthTokenResponse {

    @Schema(description = "JWT стойност; поставете в Authorization: Bearer <token>")
    private String token;

    @Schema(description = "Тип на токена; винаги Bearer", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Валидност на токена в секунди", example = "3600")
    private long expiresIn;

    @Schema(description = "Идентификатор на потребителя в UniCloud", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private UUID userId;

    @Schema(description = "Имейл адрес на потребителя", example = "student01@tu-sofia.bg")
    private String email;

    @Schema(description = "Роля на потребителя", example = "STUDENT")
    private String role;

    @Schema(description = "Дата и час на изтичане на токена (UTC)", example = "2026-11-22T23:00:00Z")
    private Instant expiresAt;
}
