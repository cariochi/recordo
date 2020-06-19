package com.cariochi.recordo.httpmock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpMock {

    private RequestMock request;
    private ResponseMock response;

}
