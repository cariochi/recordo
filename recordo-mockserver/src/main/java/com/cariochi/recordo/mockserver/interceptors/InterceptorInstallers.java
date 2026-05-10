package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.core.utils.Beans.Bean;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheInstaller;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInstaller;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInterceptor;
import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientInstaller;
import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInstaller;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@UtilityClass
public class InterceptorInstallers {

    public static Optional<? extends InterceptorInstaller<?>> findInterceptor(String beanName, ExtensionContext context) {
        final Beans beans = Beans.of(context);
        return Optional.<InterceptorInstaller<?>>empty()
                .or(() -> findInterceptor(beanName, beans))
                .or(() -> installInterceptor(beanName, beans));
    }

    private static Optional<NoOpInstaller> findInterceptor(String beanName, Beans beans) {

        final Map<Bean<?>, InterceptorInstaller<?>> allClients = findAllClients(beanName, beans);

        Map<Bean<?>, NoOpInstaller> foundInterceptors = new HashMap<>();
        allClients.forEach((bean, installer) ->
                installer.findInterceptor()
                        .map(NoOpInstaller::new)
                        .ifPresent(foundInstaller -> foundInterceptors.put(bean, foundInstaller))
        );

        Map<Bean<?>, NoOpInstaller> recordoBeans = foundInterceptors.entrySet().stream()
                .filter(e -> e.getKey().recordoBean())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (recordoBeans.size() == 1) {
            return recordoBeans.values().stream().findFirst();
        }

        if (recordoBeans.size() > 1) {
            List<String> recordoBeanNames = recordoBeans.keySet().stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple recordo http client beans found: %s".formatted(recordoBeanNames));
        }

        Map<Bean<?>, NoOpInstaller> primaryBeans = foundInterceptors.entrySet().stream()
                .filter(e -> e.getKey().primary())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (primaryBeans.size() == 1) {
            return primaryBeans.values().stream().findFirst();
        }

        if (primaryBeans.size() > 1) {
            List<String> primaryBeanNames = primaryBeans.keySet().stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple primary http client beans found: %s".formatted(primaryBeanNames));
        }

        if (foundInterceptors.size() > 1) {
            List<String> beanNames = foundInterceptors.keySet().stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple http client beans found: %s".formatted(beanNames));
        }

        return foundInterceptors.values().stream().findFirst();
    }

    private static Optional<? extends InterceptorInstaller<? extends RecordoInterceptor>> installInterceptor(String beanName, Beans beans) {

        final Map<Bean<?>, InterceptorInstaller<?>> installers = findAllClients(beanName, beans);

        Map<Bean<?>, ? extends InterceptorInstaller<?>> recordoBeans = installers.entrySet().stream()
                .filter(e -> e.getKey().recordoBean())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (recordoBeans.size() == 1) {
            return recordoBeans.values().stream().findFirst().map(InterceptorInstallers::installInterceptor);
        }

        if (recordoBeans.size() > 1) {
            List<String> recordoBeanNames = recordoBeans.keySet().stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple recordo http client beans found: %s".formatted(recordoBeanNames));
        }

        Map<Bean<?>, ? extends InterceptorInstaller<?>> primaryBeans = installers.entrySet().stream()
                .filter(e -> e.getKey().primary())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (primaryBeans.size() == 1) {
            return primaryBeans.values().stream().findFirst().map(InterceptorInstallers::installInterceptor);
        }

        if (primaryBeans.size() > 1) {
            List<String> primaryBeanNames = primaryBeans.keySet().stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple primary http client beans found: %s".formatted(primaryBeanNames));
        }

        if (installers.size() > 1) {
            List<String> beanNames = installers.keySet().stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple http client beans found: %s".formatted(beanNames));
        }

        return installers.values().stream().findFirst().map(InterceptorInstallers::installInterceptor);
    }

    private static Map<Bean<?>, InterceptorInstaller<?>> findAllClients(String beanName, Beans beans) {
        Map<Bean<?>, InterceptorInstaller<?>> installers = new HashMap<>();
        installers.putAll(restClientInstallers(beans));
        installers.putAll(restTemplateInstallers(beans));
        installers.putAll(okhttpInstallers(beans));
        installers.putAll(apacheInstallers(beans));
        return filterByName(installers, beanName);
    }

    private static Map<Bean<RestClient>, RestClientInstaller> restClientInstallers(Beans beans) {
        try {
            return beans.findAll(RestClient.class).stream().collect(toMap(bean -> bean, bean -> new RestClientInstaller(bean.instance())));
        } catch (NoClassDefFoundError e) {
            return Map.of();
        }
    }

    private static Map<Bean<RestTemplate>, RestTemplateInstaller> restTemplateInstallers(Beans beans) {
        try {
            return beans.findAll(RestTemplate.class).stream().collect(toMap(bean -> bean, bean -> new RestTemplateInstaller(bean.instance())));
        } catch (NoClassDefFoundError e) {
            return Map.of();
        }
    }

    private static Map<Bean<OkHttpClient>, OkhttpInstaller> okhttpInstallers(Beans beans) {
        try {
            return beans.findAll(OkHttpClient.class).stream().collect(toMap(bean -> bean, bean -> new OkhttpInstaller(bean.instance())));
        } catch (NoClassDefFoundError e) {
            return Map.of();
        }
    }

    private static Map<Bean<HttpClient>, ApacheInstaller> apacheInstallers(Beans beans) {
        try {
            return beans.findAll(HttpClient.class).stream().collect(toMap(bean -> bean, bean -> new ApacheInstaller(bean.instance())));
        } catch (NoClassDefFoundError e) {
            return Map.of();
        }
    }

    private static Map<Bean<?>, InterceptorInstaller<?>> filterByName(Map<Bean<?>, InterceptorInstaller<?>> installers, String beanName) {
        if (isEmpty(beanName)) {
            return installers;
        }
        return installers.entrySet().stream()
                .filter(entry -> entry.getKey().name().equals(beanName))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static InterceptorInstaller<? extends RecordoInterceptor> installInterceptor(InterceptorInstaller<?> installer) {
        if (installer instanceof RestClientInstaller restClientInstaller) {
            return restClientInstaller.install(new RestClientInterceptor());
        } else if (installer instanceof RestTemplateInstaller restTemplateInstaller) {
            return restTemplateInstaller.install(new RestTemplateInterceptor());
        } else if (installer instanceof OkhttpInstaller okhttpInstaller) {
            return okhttpInstaller.install(new OkhttpInterceptor());
        } else if (installer instanceof ApacheInstaller apacheInstaller) {
            return apacheInstaller.install(new ApacheInterceptor());
        } else {
            throw new IllegalStateException("Unsupported installer type: %s".formatted(installer.getClass().getName()));
        }
    }

}
