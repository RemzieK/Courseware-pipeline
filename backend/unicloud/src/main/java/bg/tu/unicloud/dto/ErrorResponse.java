package bg.tu.unicloud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартен отговор при грешка")
public class ErrorResponse {

    @Schema(description = "HTTP статус код", example = "404")
    private int status;

    @Schema(description = "Кратко описание на грешката", example = "Not Found")
    private String error;

    @Schema(description = "Подробно съобщение за грешката", example = "Курсът с дадения идентификатор не съществува.")
    private String message;

    @Schema(description = "Пътят на заявката, предизвикала грешката", example = "/api/v1/courses/3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String path;

    @Schema(description = "Дата и час на грешката (UTC)", example = "2026-11-22T19:00:00Z")
    private Instant timestamp;

    @Schema(description = "Детайли за валидационни грешки по полета; присъства само при статус 400")
    private List<FieldErrorDetail> fields;
}
