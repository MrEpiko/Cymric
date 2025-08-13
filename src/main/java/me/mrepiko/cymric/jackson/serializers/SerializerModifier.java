package me.mrepiko.cymric.jackson.serializers;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;

import java.util.List;

public class SerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            writer.assignNullSerializer(getDefaultNullSerializer(writer));
        }
        return beanProperties;
    }

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        Class<?> beanClass = beanDesc.getBeanClass();
        if (DiscordBot.getInstance() == null) {
            return serializer;
        }

        if (beanClass.isAnnotationPresent(SupportsDefaultOverriding.class)) {
            return new DefaultObjectSerializer((JsonSerializer<Object>) serializer);
        }

        return serializer;
    }

    @Override
    public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config, CollectionType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return modifySerializer(config, beanDesc, serializer);
    }

    private JsonSerializer<Object> getDefaultNullSerializer(BeanPropertyWriter writer) {
        Class<?> type = writer.getType().getRawClass();

        if (type == String.class) {
            return new DefaultNullSerializer("");
        }
        else if (type == Double.class || type == double.class || type == Float.class || type == float.class) {
            return new DefaultNullSerializer(0.0);
        }
        else if (Number.class.isAssignableFrom(type)) {
            return new DefaultNullSerializer(0);
        }
        else if (type == Boolean.class) {
            return new DefaultNullSerializer(false);
        }
        else if (List.class.isAssignableFrom(type)) {
            return new NullListSerializer();
        } else if (Enum.class.isAssignableFrom(type)) {
            return new DefaultNullSerializer("");
        }
        else {
            return NullSerializer.instance;
        }
    }

}
