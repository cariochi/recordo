package com.cariochi.recordo.junit5;

import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.given.GivenFileReader;
import com.cariochi.recordo.handler.CompositeAnnotationHandler;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.verify.Verifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;

import static com.cariochi.recordo.utils.Properties.fileName;
import static com.cariochi.recordo.utils.Properties.givenFileNamePattern;
import static org.slf4j.LoggerFactory.getLogger;

public class RecordoExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Logger log = getLogger(RecordoExtension.class);

    private final CompositeAnnotationHandler interceptor = new CompositeAnnotationHandler();
    private final GivenFileReader givenFileReader = new GivenFileReader();
    private final Verifier verifier = new Verifier();

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.beforeTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.afterTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext context) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Given.class) || parameterContext.isAnnotated(Verify.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.findAnnotation(Given.class)
                .map(given -> processGiven(given, parameterContext, extensionContext))
                .map(Optional::of)
                .orElseGet(() ->
                        parameterContext.findAnnotation(Verify.class)
                                .map(verify -> processVerify(verify, parameterContext, extensionContext))
                )
                .orElse(null);
    }

    public Object processGiven(Given given, ParameterContext parameterContext, ExtensionContext extensionContext) {
        final Class<?> testClass = extensionContext.getTestClass().get();
        final Method method = extensionContext.getTestMethod().get();
        final String pattern = givenFileNamePattern(given.file());
        final String parameterName = Optional.of(given.value())
                .filter(StringUtils::isNotBlank)
                .orElseGet(parameterContext.getParameter()::getName);
        final Type parameterType = parameterContext.getParameter().getParameterizedType();
        final String fileName = fileName(pattern, testClass, method.getName(), parameterName);
        final JsonConverter jsonConverter = JsonConverters.find(extensionContext.getTestInstance());
        return givenFileReader.readFromFile(fileName, parameterType, parameterName, jsonConverter);
    }

    public Object processVerify(Verify verify, ParameterContext parameterContext, ExtensionContext extensionContext) {
        return (Consumer<Object>) actual ->
                verifier.verify(
                        actual,
                        verify,
                        extensionContext.getTestInstance().get(),
                        extensionContext.getTestMethod().get(),
                        parameterContext.getParameter().getName()
                );
    }

}
