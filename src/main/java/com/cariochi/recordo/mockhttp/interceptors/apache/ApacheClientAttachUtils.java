package com.cariochi.recordo.mockhttp.interceptors.apache;

import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import lombok.experimental.UtilityClass;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.impl.execchain.MainClientExec;

@UtilityClass
public class ApacheClientAttachUtils {

    public RecordExecChain attachRecordExecChain(Object target) {
        return Fields.of(target).withType(ClientExecChain.class).stream()
                .findAny()
                .map(ApacheClientAttachUtils::getRecordExecChain)
                .orElse(null);
    }

    public PlaybackExecChain attachPlaybackExecChain(Object target) {
        return Fields.of(target).withType(ClientExecChain.class).stream()
                .findAny()
                .map(ApacheClientAttachUtils::getPlaybackExecChain)
                .orElse(null);
    }

    private PlaybackExecChain getPlaybackExecChain(TargetField field) {
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

    private RecordExecChain getRecordExecChain(TargetField field) {
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
