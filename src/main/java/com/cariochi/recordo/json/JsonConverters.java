package com.cariochi.recordo.json;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonConverters {

    public JsonConverter find(Object testInstance) {
        return Fields.of(testInstance)
                .withTypeAndAnnotation(ObjectMapper.class, EnableRecordo.class).stream().findAny()
                .map(TargetField::getValue)
                .map(ObjectMapper.class::cast)
                .map(JsonConverter::new)
                .orElseGet(JsonConverter::new);
    }

}
