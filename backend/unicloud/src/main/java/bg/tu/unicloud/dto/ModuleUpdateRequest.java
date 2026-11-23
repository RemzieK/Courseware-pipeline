package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Данни за редакция на съществуващ модул")
public class ModuleUpdateRequest {

    @NotBlank(message = "Заглавието на модула е задължително.")
    @Size(max = 200, message = "Заглавието не може да надвишава 200 символа.")
    @Schema(description = "Ново заглавие на модула (макс. 200 символа)", example = "Седмица 1 – Преработено въведение", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Ново описание на модула")
    private String description;

    @Min(value = 0, message = "Поредният номер трябва да бъде по-голям или равен на 0.")
    @Schema(description = "Нова позиция в курса (0-базирана)", example = "1")
    private Integer orderIndex;
}
