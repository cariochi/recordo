package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.reflecto.fields.TargetField;
import lombok.experimental.UtilityClass;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.impl.execchain.MainClientExec;

import static com.cariochi.reflecto.Reflecto.reflect;

@UtilityClass
public class ApacheClientAttachUtils {

    public static OnRequestExecChain attachOnRequestExecChain(Object target) {
        return reflect(target).fields().stream()
                .filter(field -> field.type().is(ClientExecChain.class))
                .findAny()
                .map(ApacheClientAttachUtils::getOnRequestExecChain)
                .orElse(null);
    }

    public static OnResponseExecChain attachOnResponseExecChain(Object target) {
        return reflect(target).fields().stream()
                .filter(field -> field.type().is(ClientExecChain.class))
                .findAny()
                .map(ApacheClientAttachUtils::getOnResponseExecChain)
                .orElse(null);
    }

    private OnRequestExecChain getOnRequestExecChain(TargetField field) {
        final Object value = field.getValue();
        if (value == null) {
            return null;
        } else if (OnRequestExecChain.class.isAssignableFrom(value.getClass())) {
            return (OnRequestExecChain) value;
        } else if (MainClientExec.class.isAssignableFrom(value.getClass())) {
            final OnRequestExecChain onRequestExecChain = new OnRequestExecChain((MainClientExec) value);
            field.setValue(onRequestExecChain);
            return onRequestExecChain;
        } else {
            return attachOnRequestExecChain(value);
        }
    }

    private OnResponseExecChain getOnResponseExecChain(TargetField field) {
        final Object value = field.getValue();
        if (value == null) {
            return null;
        } else if (OnResponseExecChain.class.isAssignableFrom(value.getClass())) {
            return (OnResponseExecChain) value;
        } else {
            final OnResponseExecChain onResponseExecChain = new OnResponseExecChain((ClientExecChain) value);
            field.setValue(onResponseExecChain);
            return onResponseExecChain;
        }
    }

}
