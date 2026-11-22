package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данни за редакция на съществуващ курс")
public class CourseUpdateRequest {

    @NotBlank(message = "Заглавието на курса е задължително.")
    @Size(max = 200, message = "Заглавието не може да надвишава 200 символа.")
    @Schema(description = "Ново заглавие на курса (макс. 200 символа)", example = "Въведение в програмирането – актуализирано", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Дисциплината е задължителна.")
    @Size(max = 100, message = "Дисциплината не може да надвишава 100 символа.")
    @Schema(description = "Академична дисциплина (макс. 100 символа)", example = "Информатика", requiredMode = Schema.RequiredMode.REQUIRED)
    private String discipline;

    @NotBlank(message = "Семестърът е задължителен.")
    @Size(max = 20, message = "Семестърът не може да надвишава 20 символа.")
    @Schema(description = "Академичен семестър (макс. 20 символа)", example = "2024-Есен", requiredMode = Schema.RequiredMode.REQUIRED)
    private String semester;

    @Schema(description = "Ново описание на курса")
    private String description;

    @Schema(description = "Флаг за архивиране/разархивиране на курса", example = "false")
    private Boolean archived;
}
