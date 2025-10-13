package me.mrepiko.cymric.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import lombok.Getter;
import me.mrepiko.cymric.jackson.serializers.EnvBeanDeserializerModifier;
import me.mrepiko.cymric.jackson.serializers.SerializerModifier;
import me.mrepiko.cymric.jackson.serializers.SnowflakeSerializer;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class JacksonUtils {

    @Getter
    private static final ObjectMapper defaultMapper = createObjectMapperWithModifiedSerializers();
    @Getter
    private static final ObjectMapper defaultMapperWithoutModifiedSerializers = createObjectMapperWithoutModifiedSerializers();

    private static ObjectMapper createObjectMapperWithModifiedSerializers() {
        ObjectMapper mapper = createObjectMapperWithoutModifiedSerializers();

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new SerializerModifier());
        module.setDeserializerModifier(new EnvBeanDeserializerModifier());
        module.addSerializer(ISnowflake.class, new SnowflakeSerializer());
        mapper.registerModule(module);
        return mapper;
    }

    private static ObjectMapper createObjectMapperWithoutModifiedSerializers() {
        ObjectMapper mapper = new ObjectMapper();
        applyDefaultsToMapper(mapper);
        return mapper;
    }

    private static void applyDefaultsToMapper(@NotNull ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        DefaultPrettyPrinter printer = getDefaultPrettyPrinter();
        mapper.setDefaultPrettyPrinter(printer);
    }

    @NotNull
    private static DefaultPrettyPrinter getDefaultPrettyPrinter() {
        DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter() {
            @Override
            public void writeObjectFieldValueSeparator(com.fasterxml.jackson.core.JsonGenerator g) throws IOException {
                g.writeRaw(": ");
            }

            @Override
            public DefaultPrettyPrinter createInstance() {
                return this;
            }
        };
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        return printer;
    }

    /**
     * Creates a deep copy of the given object using the default ObjectMapper.
     *
     * @param value The object to be copied.
     * @return A deep copy of the object.
     * @throws RuntimeException if the deep copy fails.
     */
    @NotNull
    public static Object deepCopy(@NotNull Object value) throws IOException {
        Class<?> clazz = value.getClass();
        ObjectMapper mapper = getDefaultMapper();
        TokenBuffer buffer = new TokenBuffer(mapper, false);
        mapper.writeValue(buffer, value);
        return mapper.readValue(buffer.asParser(), clazz);
    }

    /**
     * Creates a new ObjectNode using the default ObjectMapper.
     *
     * @return ObjectNode A new ObjectNode instance.
     */
    public static ObjectNode createObjectNode() {
        return defaultMapper.createObjectNode();
    }

    /**
     * Creates a new ArrayNode using the default ObjectMapper.
     *
     * @return ArrayNode A new ArrayNode instance.
     */
    public static ArrayNode createArrayNode() {
        return defaultMapper.createArrayNode();
    }

    /**
     * Creates a new JsonContainer with an empty ObjectNode.
     *
     * @return JsonContainer A new JsonContainer instance with an empty ObjectNode.
     */
    public static JsonContainer createJsonContainer() {
        return new JsonContainer(createObjectNode());
    }

    public static void mergeDeclaredFieldsFromJson(@NotNull Object target, @NotNull JsonContainer config) {
        mergeDeclaredFieldsFromJson(target, config, true);
    }

    /**
     * Merges declared fields from the given JSON configuration into the target object.
     * If a field is present in the JSON, it will be set on the target object.
     * If a field is not present in the JSON, its current value will be added to the JSON.
     *
     * @param target The target object to merge fields into.
     * @param config The JSON configuration containing field values.
     */
    public static void mergeDeclaredFieldsFromJson(@NotNull Object target, @NotNull JsonContainer config, boolean setConfigIfMissing) {
        ObjectMapper mapper = JacksonUtils.getDefaultMapper();
        Class<?> clazz = target.getClass();

        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(clazz));

        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            AnnotatedField annotatedField = prop.getField();
            if (annotatedField == null) {
                continue;
            }

            Field field = annotatedField.getAnnotated();
            if (!field.getDeclaringClass().equals(clazz)) {
                continue;
            }
            if (mapper.getSerializationConfig().getAnnotationIntrospector().hasIgnoreMarker(annotatedField)) {
                continue;
            }

            String jsonName = prop.getName();
            try {
                field.setAccessible(true);

                if (field.isAnnotationPresent(JsonUnwrapped.class)) {
                    Object nestedObj = field.get(target);
                    if (nestedObj == null) {
                        nestedObj = field.getType().getDeclaredConstructor().newInstance();
                        field.set(target, nestedObj);
                    }
                    mergeDeclaredFieldsFromJson(nestedObj, config, setConfigIfMissing);
                    continue;
                }

                if (config.has(jsonName)) {
                    JavaType genericJavaType = mapper.getTypeFactory().constructType(field.getGenericType());
                    Object value = mapper.convertValue(config.getJsonNode(jsonName), genericJavaType);
                    field.set(target, value);
                } else if (setConfigIfMissing) {
                    Object currentValue = field.get(target);
                    config.set(jsonName, currentValue);
                    field.set(target, config.get(jsonName, field.getType()));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process field: " + field.getName(), e);
            }
        }
    }

}
