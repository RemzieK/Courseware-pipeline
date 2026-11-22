package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Страница от резултати с метаданни за пагинация")
public class PagedResponse<T> {

    @Schema(description = "Съдържание на текущата страница")
    private List<T> content;

    @Schema(description = "Номер на текущата страница (0-базиран)", example = "0")
    private int page;

    @Schema(description = "Брой елементи на страница", example = "20")
    private int size;

    @Schema(description = "Общ брой елементи в резултата (всички страници)", example = "143")
    private long totalElements;

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }
}
