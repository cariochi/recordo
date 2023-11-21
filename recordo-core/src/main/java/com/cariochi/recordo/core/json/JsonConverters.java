package com.cariochi.recordo.core.json;

import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.core.utils.Beans.OptionalBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@UtilityClass
public class JsonConverters {

    public static JsonConverter getJsonConverter(String beanName, ExtensionContext context) {
        final OptionalBean<ObjectMapper> optionalBean = Beans.of(context).find(beanName, ObjectMapper.class);
        if (isNotEmpty(beanName) && optionalBean.value().isEmpty()) {
            throw new IllegalArgumentException(format("No ObjectMapper bean named '%s' available. Available beans: %s", beanName, optionalBean.availableBeanNames()));
        }
        return optionalBean.value()
                .map(JsonConverter::new)
                .orElseGet(JsonConverter::new);
    }

}
