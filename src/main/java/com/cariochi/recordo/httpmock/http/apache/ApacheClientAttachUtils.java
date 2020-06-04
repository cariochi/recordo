package com.cariochi.recordo.httpmock.http.apache;

import com.cariochi.recordo.utils.Fields;
import com.cariochi.recordo.utils.Fields.ObjectField;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.impl.execchain.MainClientExec;

public final class ApacheClientAttachUtils {

    private ApacheClientAttachUtils() {
    }

    public static RecordExecChain attachRecordExecChain(Object target) {
        return Fields.getAllFields(target).stream()
                .filter(field -> ClientExecChain.class.isAssignableFrom(field.getFieldClass()))
                .findAny()
                .map(ApacheClientAttachUtils::getRecordExecChain)
                .orElse(null);
    }

    public static PlaybackExecChain attachPlaybackExecChain(Object target) {
        return Fields.getAllFields(target).stream()
                .filter(field -> ClientExecChain.class.isAssignableFrom(field.getFieldClass()))
                .findAny()
                .map(ApacheClientAttachUtils::getPlaybackExecChain)
                .orElse(null);
    }

    public static PlaybackExecChain getPlaybackExecChain(ObjectField field) {
        final Object value = field.getValue();
        if (value == null) {
            return null;
        } else if (PlaybackExecChain.class.isAssignableFrom(value.getClass())) {
            return (PlaybackExecChain) value;
        } else if (MainClientExec.class.isAssignableFrom(value.getClass())) {
            final PlaybackExecChain playbackExecChain = new PlaybackExecChain((MainClientExec) value);
            field.setValue(playbackExecChain);
            return playbackExecChain;
        } else {
            return attachPlaybackExecChain(value);
        }
    }

    private static RecordExecChain getRecordExecChain(ObjectField field) {
        final Object value = field.getValue();
        if (value == null) {
            return null;
        } else if (RecordExecChain.class.isAssignableFrom(value.getClass())) {
            return (RecordExecChain) value;
        } else {
            final RecordExecChain recordExecChain = new RecordExecChain((ClientExecChain) value);
            field.setValue(recordExecChain);
            return recordExecChain;
        }
    }
}
