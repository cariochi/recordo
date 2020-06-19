package com.cariochi.recordo.httpmock.http;

import com.cariochi.recordo.httpmock.http.apache.PlaybackExecChain;
import com.cariochi.recordo.httpmock.http.apache.RecordExecChain;
import com.cariochi.recordo.httpmock.model.RequestMock;
import com.cariochi.recordo.httpmock.model.ResponseMock;
import org.apache.http.client.HttpClient;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.cariochi.recordo.httpmock.http.apache.ApacheClientAttachUtils.attachPlaybackExecChain;
import static com.cariochi.recordo.httpmock.http.apache.ApacheClientAttachUtils.attachRecordExecChain;

public class ApacheHttpClientInterceptor implements HttpClientInterceptor {

    private final PlaybackExecChain playbackExecChain;
    private final RecordExecChain recordExecChain;

    public ApacheHttpClientInterceptor(HttpClient httpClient) {
        playbackExecChain = attachPlaybackExecChain(httpClient);
        recordExecChain = attachRecordExecChain(httpClient);
    }

    @Override
    public void init(Function<RequestMock, Optional<ResponseMock>> onBeforeRequest,
                     BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest) {

        playbackExecChain.init(request -> {
            final Optional<ResponseMock> response = onBeforeRequest.apply(request);
            recordExecChain.setActive(!response.isPresent());
            return response;
        });

        recordExecChain.init(onAfterRequest);
    }

}
