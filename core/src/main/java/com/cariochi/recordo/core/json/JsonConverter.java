package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JsonConverter {

    private final ObjectMapper objectMapper;
    private final JacksonPrinter printer = new JacksonPrinter();

    public JsonConverter() {
        this(
                new ObjectMapper()
                        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                        .registerModule(new JavaTimeModule())
                        .setDateFormat(new StdDateFormat())
        );
    }

    public String toJson(Object object) {
        return toJson(object, null);
    }

    @SneakyThrows
    public String toJson(Object object, JsonPropertyFilter filter) {
        return object == null || object instanceof String
                ? (String) object
                : objectMapper(filter).writer(printer).writeValueAsString(object);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Type type) {
        if (json == null) {
            return null;
        }
        if (String.class.equals(type)) {
            return (T) json;
        }
        final JavaType valueType = objectMapper.constructType(type);
        return objectMapper.readValue(json, valueType);
    }

    public ObjectMapper objectMapper(JsonPropertyFilter propertyFilter) {
        return Optional.ofNullable(propertyFilter)
                .filter(JsonPropertyFilter::hasProperties)
                .map(RecordoFilter::new)
                .map(filter -> new SimpleFilterProvider().addFilter(RecordoFilter.NAME, filter))
                .map(provider -> objectMapper.copy().setFilterProvider(provider))
                .map(mapper -> mapper.addMixIn(Object.class, PropertyFilterMixIn.class))
                .orElse(objectMapper);
    }

    @RequiredArgsConstructor
    static class RecordoFilter extends SimpleBeanPropertyFilter {

        public static final String NAME = "recordo-filter";

        private final JsonPropertyFilter filter;

        @Override
        public void serializeAsField(Object pojo,
                                     JsonGenerator jgen,
                                     SerializerProvider provider,
                                     PropertyWriter writer) throws Exception {
            String path = path(jgen.getOutputContext().getParent(), writer.getName());
            if (filter.shouldInclude(path)) {
                super.serializeAsField(pojo, jgen, provider, writer);
            }
        }

        private String path(JsonStreamContext context, String field) {
            final List<String> path = new ArrayList<>();
            JsonStreamContext current = context;
            while (current != null) {
                if (current.getCurrentName() != null) {
                    path.add(0, current.getCurrentName());
                }
                current = current.getParent();
            }
            path.add(field);
            return String.join(".", path);
        }

    }


    @JsonFilter(RecordoFilter.NAME)
    static class PropertyFilterMixIn {
    }

}

