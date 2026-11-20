package bg.tu.unicloud.dto;

import jakarta.validation.constraints.Min;
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
public class LessonUpdateRequest {

    @NotBlank(message = "Заглавието на занятието е задължително.")
    @Size(max = 200, message = "Заглавието не може да надвишава 200 символа.")
    private String title;

    private String description;

    @Min(value = 0, message = "Поредният номер трябва да бъде по-голям или равен на 0.")
    private Integer orderIndex;
}
