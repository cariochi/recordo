package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import static com.cariochi.reflecto.Reflecto.reflect;

public class OkhttpInstaller implements InterceptorInstaller {

    private final OkHttpClient httpclient;
    private OkhttpRecordoInterceptor interceptor;

    public OkhttpInstaller(OkHttpClient httpClient) {
        this.httpclient = httpClient;
    }

    public OkhttpInstaller install(OkhttpRecordoInterceptor interceptor) {
        this.interceptor = interceptor;
        final List<Interceptor> interceptors = new ArrayList<>(httpclient.interceptors());
        interceptors.add(interceptor);
        reflect(httpclient).fields().get("interceptors").setValue(interceptors);
        return this;
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        this.interceptor.init(handler);
    }

    @Override
    public void uninstall() {
        httpclient.interceptors().remove(interceptor);
    }
}
