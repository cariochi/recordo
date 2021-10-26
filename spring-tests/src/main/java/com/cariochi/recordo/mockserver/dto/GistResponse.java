package com.cariochi.recordo.mockserver.dto;

import lombok.Data;
import lombok.Singular;

import java.time.Instant;
import java.util.Map;

@Data
public class GistResponse {

    private String id;

    private String description;

    private String url;

    private String html_url;

    @Singular
    private Map<String, Map<String, String>> files;

    private Instant created_at;

    private Instant updated_at;

}
