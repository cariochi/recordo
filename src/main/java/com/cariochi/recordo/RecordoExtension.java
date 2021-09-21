package com.cariochi.recordo;

import com.cariochi.recordo.mockmvc.resolvers.*;
import com.cariochi.recordo.mockserver.MockServerAnnotationHandler;
import com.cariochi.recordo.read.ReadAnnotationHandler;
import com.cariochi.recordo.read.ReadParameterResolver;
import com.cariochi.recordo.typeref.ParameterizedTypeReferenceParameterResolver;
import com.cariochi.recordo.typeref.TypeRefParameterResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.util.List;

import static com.cariochi.recordo.utils.exceptions.Exceptions.tryAccept;
import static java.util.Arrays.asList;

@Slf4j
public class RecordoExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final List<Extension> handlers = asList(
            new ReadAnnotationHandler(),
            new MockServerAnnotationHandler()
    );

    private final List<ParameterResolver> parameterResolvers = asList(
            new ReadParameterResolver(),
            new RequestParameterResolver(),
            new GetParameterResolver(),
            new PostParameterResolver(),
            new PutParameterResolver(),
            new PatchParameterResolver(),
            new DeleteParameterResolver(),
            new MockMvcClientParameterResolver(),
            new TypeRefParameterResolver(),
            new ParameterizedTypeReferenceParameterResolver()
    );

    @Override
    public void beforeEach(ExtensionContext context) {
        handlers.stream()
                .filter(i -> BeforeEachCallback.class.isAssignableFrom(i.getClass()))
                .map(BeforeEachCallback.class::cast)
                .forEach(tryAccept(processor -> processor.beforeEach(context)));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        handlers.stream()
                .filter(i -> AfterEachCallback.class.isAssignableFrom(i.getClass()))
                .map(AfterEachCallback.class::cast)
                .forEach(tryAccept(processor -> processor.afterEach(context)));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameterResolvers.stream()
                .anyMatch(r -> r.supportsParameter(parameter, extension));
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        return parameterResolvers.stream()
                .filter(r -> r.supportsParameter(parameter, extension))
                .findFirst()
                .map(r -> r.resolveParameter(parameter, extension))
                .orElse(null);
    }

}
