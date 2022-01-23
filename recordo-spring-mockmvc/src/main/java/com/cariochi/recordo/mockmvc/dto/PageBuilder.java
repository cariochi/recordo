package com.cariochi.recordo.mockmvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageBuilder<T> {

    private List<T> content = new ArrayList<>();
    private Integer number;
    private Integer size;
    private Long totalElements;

    public Page<T> build() {
        return new PageImpl<>(
                content,
                size == 0 ? Pageable.unpaged() : PageRequest.of(number, size),
                totalElements
        );
    }

}
