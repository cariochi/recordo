package com.cariochi.recordo.json;

import java.lang.reflect.Type;

public interface JsonConverter {

    String toJson(Object object, JsonPropertyFilter filter);

    Object fromJson(String json, Type type);

}
