package com.cariochi.recordo.mockserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Gist {

    private String description;

    @Singular
    private Map<String, File> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class File {
        private String content;
    }
}
