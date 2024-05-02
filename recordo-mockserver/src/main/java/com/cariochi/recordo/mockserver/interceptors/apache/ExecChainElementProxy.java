package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.fields.TargetField;
import com.cariochi.reflecto.invocations.model.Reflection;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.ExecChainHandler;

import static com.cariochi.reflecto.Reflecto.reflect;

@Getter
@RequiredArgsConstructor
public class ExecChainElementProxy {

    private final Object execChainElement;

    public static ExecChainElementProxy create(Object target) {
        Object original = reflect(target).fields().stream()
                .filter(field -> field.type().getTypeName().equals("org.apache.hc.client5.http.impl.classic.ExecChainElement"))
                .findAny()
                .map(TargetField::getValue)
                .orElse(null);

        return new ExecChainElementProxy(original);
    }

    public Optional<ExecChainHandler> getHandler() {
        return findHandlerField().map(TargetField::getValue);
    }

    public void setHandler(ExecChainHandler handler) {
        findHandlerField().ifPresent(targetField -> targetField.setValue(handler));
    }

    private Optional<TargetField> findHandlerField() {
        return Optional.ofNullable(execChainElement)
                .map(Reflecto::reflect)
                .map(Reflection::fields)
                .flatMap(targetFields -> targetFields.find("handler"));
    }

}
