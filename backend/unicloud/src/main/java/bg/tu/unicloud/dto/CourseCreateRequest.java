package bg.tu.unicloud.dto;

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
public class CourseCreateRequest {

    @NotBlank(message = "Кодът на курса е задължителен.")
    @Size(max = 30, message = "Кодът не може да надвишава 30 символа.")
    private String code;

    @NotBlank(message = "Заглавието на курса е задължително.")
    @Size(max = 200, message = "Заглавието не може да надвишава 200 символа.")
    private String title;

    @NotBlank(message = "Дисциплината е задължителна.")
    @Size(max = 100, message = "Дисциплината не може да надвишава 100 символа.")
    private String discipline;

    @NotBlank(message = "Семестърът е задължителен.")
    @Size(max = 20, message = "Семестърът не може да надвишава 20 символа.")
    private String semester;

    private String description;

    private Long externalCourseOfferingId;
}
