package bg.tu.unicloud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Детайл за валидационна грешка в конкретно поле")
public class FieldErrorDetail {

    @Schema(description = "Наименование на полето, съдържащо невалидна стойност", example = "title")
    private String field;

    @Schema(description = "Съобщение за валидационната грешка", example = "Заглавието на курса е задължително.")
    private String message;
}
