package com.cariochi.recordo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;

@Data
@Builder
@With
@JsonInclude(NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class TestDto {

    private Integer id;
    private String text;
    private String nullValue;
    private List<String> strings;
    private Instant date;
    private TestDto parent;

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

    public TestDto withChild(TestDto child) {
        children.add(child);
        return this;
    }

}
