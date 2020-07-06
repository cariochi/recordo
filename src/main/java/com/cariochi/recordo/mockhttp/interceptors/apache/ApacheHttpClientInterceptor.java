package com.cariochi.recordo.mockhttp.interceptors.apache;

import com.cariochi.recordo.mockhttp.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockhttp.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.model.MockHttpResponse;
import org.apache.http.client.HttpClient;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ApacheHttpClientInterceptor implements HttpClientInterceptor {

    private final PlaybackExecChain playbackExecChain;
    private final RecordExecChain recordExecChain;

    public ApacheHttpClientInterceptor(HttpClient httpClient) {
        playbackExecChain = ApacheClientAttachUtils.attachPlaybackExecChain(httpClient);
        recordExecChain = ApacheClientAttachUtils.attachRecordExecChain(httpClient);
    }

    @Override
    public void init(Function<MockHttpRequest, Optional<MockHttpResponse>> onBeforeRequest,
                     BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onAfterRequest) {

        playbackExecChain.init(request -> {
            final Optional<MockHttpResponse> response = onBeforeRequest.apply(request);
            recordExecChain.setActive(!response.isPresent());
            return response;
        });

        recordExecChain.init(onAfterRequest);
    }

}
