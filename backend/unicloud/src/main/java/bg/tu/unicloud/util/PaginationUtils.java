package bg.tu.unicloud.util;

import bg.tu.unicloud.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PaginationUtils {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PaginationUtils() {}

    public static Pageable createPageable(Integer page, Integer size, String sortParam,
                                          Set<String> allowedSortFields, String defaultSortField,
                                          Sort.Direction defaultDirection) {
        int pageNumber = page != null ? page : DEFAULT_PAGE;
        if (pageNumber < 0) {
            throw new BadRequestException("Номерът на страницата (page) не може да бъде отрицателно число.");
        }

        int pageSize = size != null ? size : DEFAULT_SIZE;
        if (pageSize <= 0 || pageSize > MAX_SIZE) {
            throw new BadRequestException("Размерът на страницата (size) трябва да бъде между 1 и " + MAX_SIZE + ".");
        }

        String field = defaultSortField;
        Sort.Direction direction = defaultDirection != null ? defaultDirection : Sort.Direction.ASC;

        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            String requestedField = parts[0].trim();

            if (allowedSortFields != null && !allowedSortFields.contains(requestedField)) {
                throw new BadRequestException("Неподдържано поле за сортиране: " + requestedField);
            }
            field = requestedField;

            if (parts.length > 1) {
                String dirStr = parts[1].trim();
                if ("desc".equalsIgnoreCase(dirStr)) {
                    direction = Sort.Direction.DESC;
                } else if ("asc".equalsIgnoreCase(dirStr)) {
                    direction = Sort.Direction.ASC;
                } else {
                    throw new BadRequestException("Невалидна посока на сортиране: " + dirStr + ". Използвайте 'asc' или 'desc'.");
                }
            }
        }

        // Deterministic sorting: append secondary sort on 'id' if not already sorting by id
        Sort sort = Sort.by(direction, field);
        if (!"id".equalsIgnoreCase(field)) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
