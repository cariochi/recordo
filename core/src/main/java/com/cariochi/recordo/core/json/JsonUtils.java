package com.cariochi.recordo.core.json;

import lombok.experimental.UtilityClass;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.stream.Stream;

@UtilityClass
public class JsonUtils {

    public static JSONCompareMode compareMode(boolean extensible, boolean strictOrder) {
        return Stream.of(JSONCompareMode.values())
                .filter(mode -> mode.isExtensible() == extensible)
                .filter(mode -> mode.hasStrictOrder() == strictOrder)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Compare mode not found"));
    }

}
