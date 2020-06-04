package com.cariochi.recordo.json;

import java.lang.reflect.Type;

public interface JsonConverter {

    String toJson(Object object);

    String toJson(Object object, JsonPropertyFilter filter);

    <T> T fromJson(String json, Type type);

}
