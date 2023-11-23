package com.cariochi.recordo.mockserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.Singular;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
