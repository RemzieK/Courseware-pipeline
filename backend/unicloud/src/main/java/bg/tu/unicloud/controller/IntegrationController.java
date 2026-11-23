package bg.tu.unicloud.controller;

import bg.tu.unicloud.dto.AssignmentIntegrationResponse;
import bg.tu.unicloud.dto.PagedResponse;
import bg.tu.unicloud.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
@Tag(name = "Integrations", description = "Интеграционни точки за Екип 3 (AutoJudge)")
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping("/assignments")
    @Operation(
            summary = "Списък с условия на курсови задачи за Екип 3 (AutoJudge)",
            description = """
                    Изисква роля SERVICE или ADMIN (Bearer токен от AutoJudge).
                    Връща странициран списък с материали от тип ASSIGNMENT, опционално филтриран по courseOfferingId.
                    Всеки запис включва SHA-256 хеш за проверка на целостта (UC-05).
                    """)
    @ApiResponse(responseCode = "200", description = "Успешно извлечен списък с условия",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "401", description = "Липсващ или невалиден Bearer токен",
            content = @Content(schema = @Schema(implementation = bg.tu.unicloud.dto.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Ролята на токена не е SERVICE или ADMIN",
            content = @Content(schema = @Schema(implementation = bg.tu.unicloud.dto.ErrorResponse.class)))
    public ResponseEntity<PagedResponse<AssignmentIntegrationResponse>> getAssignments(
            @RequestParam(required = false) Long courseOfferingId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size,
            @RequestParam(required = false) String sort) {

        PagedResponse<AssignmentIntegrationResponse> response = integrationService.getAssignments(
                courseOfferingId, page, size, sort
        );

        return ResponseEntity.ok(response);
    }
}
