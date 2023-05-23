package com.cariochi.recordo.main.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Arrays.asList;

@Data
@Builder
@With
@JsonInclude(NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class TestDto {

    private Integer id;
    private String text;
    private String nullValue;
    private List<String> strings;
    private Instant date;
    private TestDto parent;
    private Set<ItemDto> itemsSet;

    @Builder.Default
    private List<TestDto> children = new ArrayList<>();

    public static TestDto dto(int id) {
        return TestDto.builder()
                .id(id)
                .text("Test Object " + id)
                .strings(asList(String.valueOf(id), String.valueOf(id + 1), String.valueOf(id + 2)))
                .date(Instant.ofEpochSecond(1577836800 + 86400L * id))
                .build();
    }

    public static ItemDto item(Integer index, String string) {
        return new ItemDto(index, string);
    }

    public TestDto withChild(TestDto child) {
        children.add(child);
        return this;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {

        private Integer index;
        private String string;

    }

}
