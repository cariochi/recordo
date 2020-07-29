package com.cariochi.recordo.mockmvc.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.springframework.data.domain.Sort.unsorted;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageDto<T> extends PageImpl<T> {

    public PageDto(T... content) {
        super(asList(content));
    }

    public PageDto(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    @JsonCreator
    public PageDto(@JsonProperty("content") List<T> content,
                   @JsonProperty("pageable") JsonNode pageable,
                   @JsonProperty("size") Integer size,
                   @JsonProperty("number") Integer number,
                   @JsonProperty("first") Boolean first,
                   @JsonProperty("last") Boolean last,
                   @JsonProperty("totalElements") Long totalElements,
                   @JsonProperty("totalPages") Integer totalPages,
                   @JsonProperty("sort") @JsonDeserialize(using = SortJsonDeserializer.class) Sort sort,
                   @JsonProperty("numberOfElements") Integer numberOfElements) {
        super(
                content,
                PageRequest.of(number, size, Optional.ofNullable(sort).orElse(unsorted())),
                totalElements
        );
    }
}
