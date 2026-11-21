package bg.tu.unicloud.util;

import bg.tu.unicloud.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationUtilsTest {

    private static final Set<String> ALLOWED_FIELDS = Set.of("id", "code", "title", "createdAt");

    @Test
    @DisplayName("Defaults to page=0 and size=20 when null")
    void testDefaults() {
        Pageable pageable = PaginationUtils.createPageable(null, null, null, ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC);

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("Rejects negative page index")
    void testNegativePage() {
        assertThatThrownBy(() -> PaginationUtils.createPageable(-1, 20, null, ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не може да бъде отрицателно число");
    }

    @Test
    @DisplayName("Rejects page size greater than 100")
    void testMaxSizeCap() {
        assertThatThrownBy(() -> PaginationUtils.createPageable(0, 101, null, ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("между 1 и 100");
    }

    @Test
    @DisplayName("Rejects page size less than or equal to 0")
    void testZeroOrNegativeSize() {
        assertThatThrownBy(() -> PaginationUtils.createPageable(0, 0, null, ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("между 1 и 100");
    }

    @Test
    @DisplayName("Rejects unsupported sort field")
    void testUnsupportedSortField() {
        assertThatThrownBy(() -> PaginationUtils.createPageable(0, 20, "unsupportedField,asc", ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Неподдържано поле за сортиране");
    }

    @Test
    @DisplayName("Rejects invalid sort direction")
    void testInvalidSortDirection() {
        assertThatThrownBy(() -> PaginationUtils.createPageable(0, 20, "title,sideways", ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Невалидна посока на сортиране");
    }

    @Test
    @DisplayName("Applies deterministic sorting with secondary id sort")
    void testDeterministicSort() {
        Pageable pageable = PaginationUtils.createPageable(1, 50, "title,asc", ALLOWED_FIELDS, "createdAt", Sort.Direction.DESC);

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        Sort.Order titleOrder = pageable.getSort().getOrderFor("title");
        Sort.Order idOrder = pageable.getSort().getOrderFor("id");

        assertThat(titleOrder).isNotNull();
        assertThat(titleOrder.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(idOrder).isNotNull();
        assertThat(idOrder.getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
