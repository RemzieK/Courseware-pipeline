package bg.tu.unicloud.annotation;

import bg.tu.unicloud.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * UC-32 – Composite annotation that documents the standard error responses
 * (400, 401, 403, 404) for mutating or secured API endpoints.
 *
 * <p>Apply this to any {@code @PostMapping}, {@code @PutMapping},
 * {@code @PatchMapping} or {@code @DeleteMapping} method to avoid repeating
 * the same four {@link ApiResponse} declarations on every endpoint.</p>
 *
 * <p>For read-only endpoints that are publicly accessible, only 404 is relevant –
 * document that inline with a single {@code @ApiResponse} rather than this annotation.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Невалидни входни данни – валидационна грешка в полетата",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Липсващ или невалиден Bearer JWT токен",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Недостатъчни права за изпълнение на операцията",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Търсеният ресурс не е намерен",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface StandardApiResponses {
}
