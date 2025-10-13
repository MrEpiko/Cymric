package me.mrepiko.cymric.jackson.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.mrepiko.cymric.annotations.ReadEnvVariables;
import me.mrepiko.cymric.mics.Utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnvBeanDeserializerModifier extends BeanDeserializerModifier {

    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        if (beanDesc.getBeanClass().isAnnotationPresent(ReadEnvVariables.class)) {
            return new EnvResolvingDeserializer((JsonDeserializer<Object>) deserializer, beanDesc.getBeanClass());
        }
        return deserializer;
    }

    public static class EnvResolvingDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {

        private final JsonDeserializer<Object> defaultDeserializer;

        public EnvResolvingDeserializer(JsonDeserializer<Object> defaultDeserializer, Class<?> beanClass) {
            super(beanClass);
            this.defaultDeserializer = defaultDeserializer;
        }

        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            if (defaultDeserializer instanceof ResolvableDeserializer rd) {
                rd.resolve(ctxt);
            }
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object obj = defaultDeserializer.deserialize(p, ctxt);
            resolveEnvVariables(obj);
            return obj;
        }

        private void resolveEnvVariables(Object obj) {
            if (obj == null) {
                return;
            }

            Class<?> clazz = obj.getClass();
            if (clazz.isPrimitive() || clazz.getName().startsWith("java.") || clazz.isEnum()) {
                return;
            }

            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value == null) continue;

                    if (value instanceof String s) {
                        field.set(obj, Utils.resolveEnv(s));
                    } else if (value instanceof List<?> list) {
                        List<Object> newList = new ArrayList<>();
                        for (Object item : list) {
                            if (item instanceof String strItem) {
                                newList.add(Utils.resolveEnv(strItem));
                            } else {
                                resolveEnvVariables(item);
                                newList.add(item);
                            }
                        }
                        field.set(obj, newList);
                    } else if (value instanceof Map<?, ?> map) {
                        Map<Object, Object> newMap = new LinkedHashMap<>();
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            Object key = entry.getKey();
                            Object val = entry.getValue();
                            if (val instanceof String strVal) {
                                val = Utils.resolveEnv(strVal);
                            } else {
                                resolveEnvVariables(val);
                            }
                            newMap.put(key, val);
                        }
                        field.set(obj, newMap);
                    } else if (!field.getType().isPrimitive() && !field.getType().getName().startsWith("java.")) {
                        resolveEnvVariables(value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
