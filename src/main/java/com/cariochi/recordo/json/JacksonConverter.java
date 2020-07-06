package com.cariochi.recordo.json;

import com.cariochi.recordo.RecordoError;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.Optional;

@RequiredArgsConstructor
public class JacksonConverter implements JsonConverter {

    private final ObjectMapper objectMapper;

    public JacksonConverter() {
        this(new ObjectMapper().registerModule(new JavaTimeModule()).setDateFormat(new StdDateFormat()));
    }

    @Override
    public String toJson(Object object) {
        return toJson(object, null);
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        if (object == null || object instanceof String) {
            return (String) object;
        }
        try {
            return objectMapper(filter).writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RecordoError(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Type type) {
        if (json == null) {
            return null;
        }
        if (String.class.equals(type)) {
            return (T) json;
        }
        try {
            final JavaType valueType = objectMapper.getTypeFactory().constructType(type);
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RecordoError(e);
        }
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
            if (!filterOfContext(jgen.getOutputContext().getParent(), filter).shouldExclude(writer.getName())) {
                super.serializeAsField(pojo, jgen, provider, writer);
            }
        }

        private JsonPropertyFilter filterOfContext(JsonStreamContext context, JsonPropertyFilter filter) {
            if (context == null) {
                return filter;
            }
            final JsonPropertyFilter nextFilter = Optional.ofNullable(context.getCurrentName())
                    .map(filter::next)
                    .orElse(filter);
            return filterOfContext(context.getParent(), nextFilter);
        }
    }

    @JsonFilter(RecordoFilter.NAME)
    static class PropertyFilterMixIn {
    }

}

