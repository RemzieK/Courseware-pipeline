package bg.tu.unicloud.controller;

import bg.tu.unicloud.dto.PagedResponse;
import bg.tu.unicloud.dto.SearchResultDto;
import bg.tu.unicloud.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Търсене в каталога с курсове и учебни материали")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(
            summary = "Търсене по ключова дума, дисциплина, преподавател или тема",
            description = """
                    Публичен ендпойнт. Използва PostgreSQL pg_trgm за размито търсене с толеранс
                    до 1 грешна буква в кирилица (праг за сходство 0.2).
                    Резултатите включват курсове и материали, съвпадащи с подадените критерии.
                    SLO: p95 < 500 ms при тестовите данни (UC-07).
                    """)
    @ApiResponse(responseCode = "200", description = "Успешно извлечени резултати от търсенето",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    public ResponseEntity<PagedResponse<SearchResultDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {

        PagedResponse<SearchResultDto> response = searchService.search(
                q, discipline, teacher, topic, page, size, sort
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }
}
