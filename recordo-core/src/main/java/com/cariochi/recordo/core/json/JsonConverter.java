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
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JsonConverter {

    private final ObjectMapper objectMapper;
    private final ObjectMapper yamlMapper;
    private final JacksonPrinter printer = new JacksonPrinter();

    public JsonConverter() {
        final JsonMapper.Builder jsonBuilder = JsonMapper.builder()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .addModule(getJsonModule())
                .defaultDateFormat(new StdDateFormat());

        final YAMLMapper.Builder yamlBuilder = YAMLMapper.builder()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(YAMLWriteFeature.INDENT_ARRAYS_WITH_INDICATOR)
                .addModule(getJsonModule())
                .defaultDateFormat(new StdDateFormat());

        try {
            Class<?> pageImpl = Class.forName("org.springframework.data.domain.PageImpl");
            jsonBuilder.addMixIn(pageImpl, IgnoringPageable.class);
            yamlBuilder.addMixIn(pageImpl, IgnoringPageable.class);
        } catch (ClassNotFoundException ignored) {
        }

        objectMapper = jsonBuilder.build();
        yamlMapper = yamlBuilder.build();
    }

    public JsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.rebuild()
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .addModule(getJsonModule())
                .build();
        yamlMapper = YAMLMapper.builder()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(YAMLWriteFeature.INDENT_ARRAYS_WITH_INDICATOR)
                .addModule(getJsonModule())
                .defaultDateFormat(new StdDateFormat())
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

    public String toYaml(Object object) {
        return toYaml(object, null);
    }

    @SneakyThrows
    public String toYaml(Object object, JsonFilter filter) {
        if (object == null || object instanceof String) {
            return (String) object;
        }
        // YAML context doesn't support path-based filtering — filter via JSON first, then convert
        if (filter != null && filter.hasProperties()) {
            Object value = objectMapper.readValue(toJson(object, filter), Object.class);
            return yamlMapper.writeValueAsString(value);
        }
        return yamlMapper.writeValueAsString(object);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T fromYaml(String yaml, Type type) {
        if (yaml == null) {
            return null;
        }
        if (String.class.equals(type)) {
            return (T) yaml;
        }
        final JavaType valueType = yamlMapper.constructType(type);
        return yamlMapper.readValue(yaml, valueType);
    }

    @SneakyThrows
    public String yamlToJson(String yaml) {
        Object value = yamlMapper.readValue(yaml, Object.class);
        return objectMapper.writer().with(printer).writeValueAsString(value);
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
