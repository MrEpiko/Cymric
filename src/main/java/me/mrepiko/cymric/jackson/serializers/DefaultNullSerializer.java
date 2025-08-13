package me.mrepiko.cymric.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class DefaultNullSerializer extends JsonSerializer<Object> {

    private final Object defaultValue;

    public DefaultNullSerializer(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeObject(defaultValue);
    }

}