package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данни за частична редакция на учебен материал (PATCH)")
public class MaterialUpdateRequest {

    @Size(max = 255, message = "Заглавието не може да надвишава 255 символа.")
    @Schema(description = "Ново заглавие на материала (макс. 255 символа)", example = "Лекционни бележки – Седмица 1 (ревизирано)")
    private String title;

    @Schema(description = "Ново описание на материала")
    private String description;

    @Size(max = 1000, message = "URL адресът на видеото не може да надвишава 1000 символа.")
    @Schema(description = "Нов URL на видео", example = "https://www.youtube.com/watch?v=newExample")
    private String videoUrl;

    @Schema(description = "Нови тагове / теми (заменят старите изцяло)", example = "[\"алгоритми\", \"рекурсия\"]")
    private List<String> tags;

    @Schema(description = "Показване/скриване на материала от студенти", example = "false")
    private Boolean hidden;
}
