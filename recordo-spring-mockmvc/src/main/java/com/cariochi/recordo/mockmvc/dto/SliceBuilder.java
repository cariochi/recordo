package com.cariochi.recordo.mockmvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SliceBuilder<T> {

    private List<T> content = new ArrayList<>();
    private Integer number;
    private Integer size;
    boolean last;

    public Slice<T> build() {
        return new SliceImpl<>(
                content,
                size == 0 ? Pageable.unpaged() : PageRequest.of(number, size),
                !last
        );
    }

}
