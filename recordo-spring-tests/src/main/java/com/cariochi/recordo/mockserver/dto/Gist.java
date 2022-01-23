package com.cariochi.recordo.mockserver.dto;

import lombok.*;

import java.util.Map;

@Data
public class Gist {

    private String description;

    @Singular
    private Map<String, File> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class File {
        private String content;
    }
}
