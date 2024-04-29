package com.cariochi.recordo.core.json;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.constructors.ReflectoConstructor;
import com.cariochi.reflecto.types.Types;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

public class JsonConverter {

    private final ObjectMapper objectMapper;
    private final JacksonPrinter printer = new JacksonPrinter();

    public JsonConverter() {
        objectMapper = new ObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .registerModule(getJsonModule())
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat());

        if (RecordoExtension.isClassAvailable("org.springframework.data.web.config.SpringDataJacksonConfiguration$PageModule")) {
            Reflecto.reflect(Types.type("org.springframework.data.web.config.SpringDataJacksonConfiguration$PageModule")).constructors().find()
                    .map(ReflectoConstructor::newInstance)
                    .map(Module.class::cast)
                    .ifPresent(objectMapper::registerModule);
        }
    }

    public JsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .registerModule(getJsonModule());
    }

    private SimpleModule getJsonModule() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Set.class, new SortedSetJsonSerializer());
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
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

    public ObjectMapper objectMapper(JsonFilter propertyFilter) {
        return Optional.ofNullable(propertyFilter)
                .filter(JsonFilter::hasProperties)
                .map(RecordoFilter::new)
                .map(filter -> new SimpleFilterProvider().addFilter(RecordoFilter.NAME, filter))
                .map(provider -> objectMapper.copy().setFilterProvider(provider))
                .map(mapper -> mapper.addMixIn(Object.class, PropertyFilterMixIn.class))
                .orElse(objectMapper);
    }

    @RequiredArgsConstructor
    static class RecordoFilter extends SimpleBeanPropertyFilter {

        public static final String NAME = "recordo-filter";

        private final JsonFilter filter;

        @Override
        public void serializeAsField(Object pojo,
                                     JsonGenerator jgen,
                                     SerializerProvider provider,
                                     PropertyWriter writer) throws Exception {
            Path path = path(jgen.getOutputContext().getParent(), writer.getName());
            if (filter.shouldInclude(path)) {
                super.serializeAsField(pojo, jgen, provider, writer);
            }
        }

        private Path path(JsonStreamContext context, String field) {
            final List<String> path = new ArrayList<>();
            JsonStreamContext current = context;
            while (current != null) {
                if (current.hasPathSegment()) {
                    if (current.getCurrentName() != null) {
                        path.add(0, current.getCurrentName());
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

}

