package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;

@RequiredArgsConstructor
public class OkhttpInstaller implements InterceptorInstaller<OkhttpInterceptor> {

    private final OkHttpClient httpclient;
    private OkhttpInterceptor interceptor;

    @Override
    public OkhttpInstaller install(OkhttpInterceptor interceptor) {
        this.interceptor = interceptor;
        final List<Interceptor> interceptors = new ArrayList<>(httpclient.interceptors());
        interceptors.add(interceptor);
        reflect(httpclient).fields().get("interceptors").setValue(interceptors);
        return this;
    }

    @Override
    public Optional<RecordoInterceptor> findInterceptor() {
        return httpclient.interceptors().stream()
                .filter(OkhttpInterceptor.class::isInstance)
                .map(RecordoInterceptor.class::cast)
                .findFirst();
    }

    @Override
    public void setHandler(RecordoRequestHandler handler) {
        this.interceptor.setHandler(handler);
    }

    @Override
    public void uninstall() {
        httpclient.interceptors().remove(interceptor);
    }
}
