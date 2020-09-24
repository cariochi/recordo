package com.cariochi.recordo.mockhttp.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockHttpInteraction {

    private MockHttpRequest request;
    private MockHttpResponse response;

}
