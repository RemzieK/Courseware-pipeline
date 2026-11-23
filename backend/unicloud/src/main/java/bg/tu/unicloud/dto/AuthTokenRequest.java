package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данни за издаване на тестов JWT токен (само за разработка / интеграционни тестове)")
public class AuthTokenRequest {

    @Schema(description = "Потребителско име (опционален)", example = "student01")
    private String username;

    @Schema(description = "Роля на потребителя", example = "STUDENT",
            allowableValues = {"STUDENT", "TEACHER", "ADMIN", "SERVICE"})
    private String role;

    @Schema(description = "Имейл адрес", example = "student01@tu-sofia.bg")
    private String email;

    @Schema(description = "Пълно ime на потребителя", example = "Иван Иванов")
    private String fullName;

    @Schema(description = "Subject claim (UUID); генерира се автоматично ако е null")
    private String sub;

    @Schema(description = "Идентификатори на курсове, до които потребителят има достъп", example = "[1001, 1002]")
    private List<Long> courses;
}
