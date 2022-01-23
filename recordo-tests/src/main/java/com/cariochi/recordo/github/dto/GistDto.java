package com.cariochi.recordo.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GistDto {

    private String id;
    private String description;
    private String url;
    private String html_url;
    private Map<String, GistFileDto> files;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GistFileDto {

        private String filename;
        private String type;
        private String language;
        private String raw_url;
        private Integer size;

    }

}
