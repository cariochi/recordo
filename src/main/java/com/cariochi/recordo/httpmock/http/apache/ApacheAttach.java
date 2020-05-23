package com.cariochi.recordo.httpmock.http.apache;

import com.cariochi.recordo.RecordoError;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.impl.execchain.MainClientExec;

import java.lang.reflect.Field;
import java.util.Optional;

public final class ApacheAttach {

    private ApacheAttach() {
    }

    public static RecordExecChain attachRecordExecChain(HttpClient httpClient) {
        try {
            final Optional<Field> optionalField = FieldUtils.getAllFieldsList(httpClient.getClass()).stream()
                    .filter(field -> ClientExecChain.class.isAssignableFrom(field.getType()))
                    .findAny();
            if (optionalField.isPresent()) {
                final Field field = optionalField.get();
                field.setAccessible(true);
                final Object value = field.get(httpClient);
                if (RecordExecChain.class.isAssignableFrom(value.getClass())) {
                    return (RecordExecChain) value;
                } else {
                    final RecordExecChain recordExecChain = new RecordExecChain((ClientExecChain) value);
                    field.set(httpClient, recordExecChain);
                    return recordExecChain;
                }
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }

    public static PlaybackExecChain attachPlaybackExecChain(Object target) {
        try {
            final Optional<Field> optionalField = FieldUtils.getAllFieldsList(target.getClass()).stream()
                    .filter(field -> ClientExecChain.class.isAssignableFrom(field.getType()))
                    .findAny();
            if (optionalField.isPresent()) {
                final Field field = optionalField.get();
                field.setAccessible(true);
                final Object value = field.get(target);
                if (MainClientExec.class.isAssignableFrom(value.getClass())) {
                    final PlaybackExecChain playbackExecChain = new PlaybackExecChain((MainClientExec) value);
                    field.set(target, playbackExecChain);
                    return playbackExecChain;
                } else {
                    return attachPlaybackExecChain(value);
                }
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }
}
