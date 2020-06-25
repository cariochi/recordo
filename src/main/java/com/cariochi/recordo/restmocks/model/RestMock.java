package com.cariochi.recordo.restmocks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestMock {

    private RequestMock request;
    private ResponseMock response;

}
