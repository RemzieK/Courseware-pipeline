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
@Schema(description = "Данни за създаване на нов курс")
public class CourseCreateRequest {

    @NotBlank(message = "Кодът на курса е задължителен.")
    @Size(max = 30, message = "Кодът не може да надвишава 30 символа.")
    @Schema(description = "Уникален код на курса (макс. 30 символа)", example = "CS-101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "Заглавието на курса е задължително.")
    @Size(max = 200, message = "Заглавието не може да надвишава 200 символа.")
    @Schema(description = "Заглавие на курса (макс. 200 символа)", example = "Въведение в програмирането", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Дисциплината е задължителна.")
    @Size(max = 100, message = "Дисциплината не може да надвишава 100 символа.")
    @Schema(description = "Академична дисциплина (макс. 100 символа)", example = "Информатика", requiredMode = Schema.RequiredMode.REQUIRED)
    private String discipline;

    @NotBlank(message = "Семестърът е задължителен.")
    @Size(max = 20, message = "Семестърът не може да надвишава 20 символа.")
    @Schema(description = "Академичен семестър (макс. 20 символа)", example = "2024-Пролет", requiredMode = Schema.RequiredMode.REQUIRED)
    private String semester;

    @Schema(description = "Незадължително описание на курса")
    private String description;

    @Schema(description = "Идентификатор на офертата за курс в системата на Екип 1", example = "1001")
    private Long externalCourseOfferingId;
}
