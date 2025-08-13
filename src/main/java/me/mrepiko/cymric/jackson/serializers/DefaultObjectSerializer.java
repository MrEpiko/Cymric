package me.mrepiko.cymric.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.jackson.JacksonUtils;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultObjectSerializer extends JsonSerializer<Object> {

    private final JsonSerializer<Object> defaultSerializer;
    private final CymricApi instance = DiscordBot.getInstance();

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Class<?> clazz = value.getClass();
        Map<Class<?>, JsonNode> overrides = instance.getDefaultObjectConfig().getOverrides();

        if (equalsToDefault(value) && overrides.containsKey(clazz)) {
            gen.writeObject(overrides.get(clazz));
            return;
        }

        defaultSerializer.serialize(value, gen, serializers);
    }

    private boolean equalsToDefault(Object value) {
        try {
            Class<?> clazz = value.getClass();
            Object defaultInstance = clazz.getDeclaredConstructor().newInstance();

            ObjectMapper mapper = JacksonUtils.getDefaultMapperWithoutModifiedSerializers();
            JsonNode nodeA = mapper.valueToTree(value);
            JsonNode nodeB = mapper.valueToTree(defaultInstance);

            return nodeA.equals(nodeB);
        } catch (Exception e) {
            throw new RuntimeException("Failed deep comparison for " + value.getClass(), e);
        }
    }

}
