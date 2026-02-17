package com.mestro.common.utils;

import com.mestro.common.dto.PageResponseDTO;
import com.mestro.common.dto.PageableDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;

public class GeneralUtils {

    public static <T> PageResponseDTO<T> pageableResponse(
            List<T> content,
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean isFirst,
            boolean isLast,
            Pageable pageable) {
        return PageResponseDTO.<T>builder()
                .content(content)
                .page(number)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(isFirst)
                .last(isLast)
                .pageable(PageableDTO.builder()
                        .pageNumber(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .paged(pageable.isPaged())
                        .unpaged(pageable.isUnpaged())
                        .offset(pageable.getOffset())
                        .sort(pageable.getSort())
                        .build())
                .build();
    }
}
