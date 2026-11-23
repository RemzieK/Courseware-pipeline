package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Резюме на курс, връщан в каталог, търсене и списъци")
public class CourseResponse {

    @Schema(description = "Уникален идентификатор на курса", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Уникален код на курса", example = "CS-101")
    private String code;

    @Schema(description = "Идентификатор на офертата за курс в системата на Екип 1", example = "1001")
    private Long externalCourseOfferingId;

    @Schema(description = "Заглавие на курса", example = "Въведение в програмирането")
    private String title;

    @Schema(description = "Академична дисциплина", example = "Информатика")
    private String discipline;

    @Schema(description = "Академичен семестър", example = "2024-Пролет")
    private String semester;

    @Schema(description = "Кратко описание на съдържанието на курса")
    private String description;

    @Schema(description = "Идентификатор на преподавателя", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private UUID teacherId;

    @Schema(description = "Пълно име на преподавателя", example = "доц. д-р Иван Петров")
    private String teacherName;

    @Schema(description = "Флаг дали курсът е архивиран (само за четене)", example = "false")
    private boolean archived;

    @Schema(description = "Дата и час на създаване (UTC)", example = "2026-09-01T08:00:00Z")
    private Instant createdAt;

    @Schema(description = "Дата и час на последна промяна (UTC)", example = "2026-11-01T12:30:00Z")
    private Instant updatedAt;
}
