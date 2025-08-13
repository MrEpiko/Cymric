package me.mrepiko.cymric.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class NullListSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o == null) {
            jsonGenerator.writeStartArray();
            jsonGenerator.writeEndArray();
        } else {
            jsonGenerator.writeStartArray();
            for (Object item : (List<?>) o) {
                if (item != null) {
                    jsonGenerator.writeObject(item);
                }
            }
            jsonGenerator.writeEndArray();
        }
    }
}