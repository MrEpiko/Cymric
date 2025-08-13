package me.mrepiko.cymric.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.io.IOException;

public class SnowflakeSerializer extends JsonSerializer<ISnowflake> {

    @Override
    public void serialize(ISnowflake iSnowflake, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(iSnowflake.getId());
    }
}
