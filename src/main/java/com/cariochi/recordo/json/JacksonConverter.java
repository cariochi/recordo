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

import java.lang.reflect.Type;
import java.util.Optional;

public class JacksonConverter implements JsonConverter {

    private final ObjectMapper objectMapper;

    public JacksonConverter() {
        this(new ObjectMapper().registerModule(new JavaTimeModule()).setDateFormat(new StdDateFormat()));
    }

    public JacksonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Object object) {
        return toJson(object, null);
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        try {
            return objectMapper(filter).writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RecordoError(e);
        }
    }

    @Override
    public <T> T fromJson(String json, Type type) {
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

    static class RecordoFilter extends SimpleBeanPropertyFilter {

        public static final String NAME = "recordo-filter";

        private final JsonPropertyFilter filter;

        public RecordoFilter(JsonPropertyFilter filter) {
            this.filter = filter;
        }

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

