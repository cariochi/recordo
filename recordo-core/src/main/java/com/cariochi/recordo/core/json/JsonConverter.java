package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanSerializer;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.databind.util.StdDateFormat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JsonConverter {

    private final ObjectMapper objectMapper;
    private final JacksonPrinter printer = new JacksonPrinter();

    public JsonConverter() {
        final JsonMapper.Builder builder = JsonMapper.builder()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModule(getJsonModule())
                .defaultDateFormat(new StdDateFormat());

        try {
            Class<?> pageImpl = Class.forName("org.springframework.data.domain.PageImpl");
            builder.addMixIn(pageImpl, IgnoringPageable.class);
        } catch (ClassNotFoundException ignored) {
        }

        objectMapper = builder.build();
    }

    public JsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.rebuild()
                .addModule(getJsonModule())
                .build();
    }

    private SimpleModule getJsonModule() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Set.class, new SortedSetJsonSerializer());
        module.setSerializerModifier(new ValueSerializerModifier() {
            @Override
            public ValueSerializer<?> modifySerializer(SerializationConfig config,
                                                       BeanDescription.Supplier beanDesc,
                                                       ValueSerializer<?> serializer) {
                return serializer instanceof BeanSerializer
                        ? new MaxDepthCyclicObjectSerializer((BeanSerializer) serializer)
                        : serializer;
            }
        });
        return module;
    }

    public String toJson(Object object) {
        return toJson(object, null);
    }

    @SneakyThrows
    public String toJson(Object object, JsonFilter filter) {
        return object == null || object instanceof String
                ? (String) object
                : objectMapper(filter).writer().with(printer).writeValueAsString(object);
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

    public ObjectMapper objectMapper(JsonFilter propertyFilter) {
        return Optional.ofNullable(propertyFilter)
                .filter(JsonFilter::hasProperties)
                .map(RecordoFilter::new)
                .map(filter -> new SimpleFilterProvider().addFilter(RecordoFilter.NAME, filter))
                .map(provider -> objectMapper.rebuild()
                        .filterProvider(provider)
                        .addMixIn(Object.class, PropertyFilterMixIn.class)
                        .build())
                .orElse(objectMapper);
    }

    @RequiredArgsConstructor
    static class RecordoFilter extends SimpleBeanPropertyFilter {

        public static final String NAME = "recordo-filter";

        private final JsonFilter filter;

        @Override
        public void serializeAsProperty(Object pojo,
                                        JsonGenerator jgen,
                                        SerializationContext ctxt,
                                        PropertyWriter writer) throws Exception {
            Path path = path(jgen.streamWriteContext().getParent(), writer.getName());
            if (filter.shouldInclude(path)) {
                super.serializeAsProperty(pojo, jgen, ctxt, writer);
            }
        }

        private Path path(TokenStreamContext context, String field) {
            final List<String> path = new ArrayList<>();
            TokenStreamContext current = context;
            while (current != null) {
                if (current.hasPathSegment()) {
                    if (current.currentName() != null) {
                        path.add(0, current.currentName());
                    } else if (current.getCurrentIndex() != -1) {
                        if (current.getParent().hasPathSegment()) {
                            path.add(0, "[" + current.getCurrentIndex() + "]");
                        }
                    }
                }
                current = current.getParent();
            }
            path.add(field);
            return new Path(path);
        }

    }


    @com.fasterxml.jackson.annotation.JsonFilter(RecordoFilter.NAME)
    static class PropertyFilterMixIn {
    }

    private abstract static class IgnoringPageable {
        @JsonIgnore
        abstract Object getPageable();
    }
}
