package me.mrepiko.cymric.config.defaultobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.SerializeAs;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.config.Configurable;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

@Getter
public class DefaultObjectConfig implements Configurable {

    private final JsonContainer config;

    @Delegate
    private final DefaultObjectData data;

    private final Map<Class<?>, JsonNode> overrides = new HashMap<>();
    private final Logger logger = DiscordBot.getLogger();

    public DefaultObjectConfig() {
        this.config = new JsonContainer(new ConfigFile(Constants.DEFAULT_OBJECTS_FILE_PATH, true));
        this.data = config.getAsOrSetDefault(DefaultObjectData.class, new DefaultObjectData());
        initialize();
        this.config.set("raw_data", data.getRawData());
    }

    public void initialize() {
        Set<Class<?>> annotatedClasses = CymricApi.reflections.getTypesAnnotatedWith(SupportsDefaultOverriding.class);
        Map<String, JsonNode> rawData = new HashMap<>(data.getRawData());

        loadOverrides(rawData, annotatedClasses);
        generateDefaultsForRemainingClasses(rawData, annotatedClasses);

        data.setRawData(Utils.sortByKey(rawData));
    }

    // Load overrides from the raw data map.
    private void loadOverrides(Map<String, JsonNode> rawData, Set<Class<?>> remainingClasses) {
        for (Map.Entry<String, JsonNode> entry : new HashMap<>(rawData).entrySet()) {
            try {
                Class<?> clazz = Class.forName(entry.getKey());
                if (!remainingClasses.contains(clazz)) {
                    logger.warn("Class {} is not annotated with @SupportsDefaultOverriding", clazz.getName());
                    throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @SupportsDefaultOverriding");
                }
                overrides.put(clazz, entry.getValue());
                remainingClasses.remove(clazz);
            } catch (ClassNotFoundException e) {
                logger.error("Class {} not found", entry.getKey(), e);
                throw new RuntimeException("Class " + entry.getKey() + " not found", e);
            }
        }
    }

    // As for remaining classes, generate default instances and populate them.
    private void generateDefaultsForRemainingClasses(Map<String, JsonNode> rawData, Set<Class<?>> remainingClasses) {
        for (Class<?> clazz : remainingClasses) {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();

                if (instance instanceof Collection<?> collection) {
                    populateContainerWithDummyEntry(collection, clazz, true);
                } else if (instance instanceof Map<?, ?> map) {
                    populateContainerWithDummyEntry(map, clazz, false);
                } else {
                    initializeFields(instance, clazz);
                }

                JsonNode node = JacksonUtils.getDefaultMapper().valueToTree(instance);
                overrides.put(clazz, node);
                rawData.put(clazz.getName(), node);
            } catch (Exception e) {
                logger.warn("Failed to initialize default object for class {}: {}", clazz.getName(), e.getMessage(), e);
            }
        }
    }

    // Fields initialization and handling.

    // Iterate through all fields and initialize them.
    private void initializeFields(Object instance, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }
            initializeField(field, instance, clazz);
        }
    }

    private void initializeField(Field field, Object instance, Class<?> mainClass) throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        Object value = field.get(instance);

        if (Utils.isPrimitiveOrString(fieldType) || fieldType.isEnum()) {
            return;
        }

        // In case of collection or maps, handle them differently.
        if (Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) {
            handleContainerField(field, instance, value);
        } else {
            handleObjectField(field, instance, mainClass, fieldType, value);
        }
    }

    private void handleObjectField(Field field, Object instance, Class<?> mainClass, Class<?> fieldType, Object value) {
        try {
            SerializeAs serializeAnnotation = field.getAnnotation(SerializeAs.class);
            Class<?> actualType = fieldType;

            if (serializeAnnotation != null) {
                actualType = serializeAnnotation.value();
            }

            Object newValue;

            if (value != null) {
                if (!actualType.isInstance(value)) {
                    newValue = instantiateAndInitialize(actualType);
                    // Convert to ObjectNode if needed
                    if (fieldType.equals(com.fasterxml.jackson.databind.node.ObjectNode.class) && !fieldType.isInstance(newValue)) {
                        newValue = JacksonUtils.getDefaultMapper().valueToTree(newValue);
                    }
                    field.set(instance, newValue);
                } else {
                    initializeFields(value, actualType);
                }
            } else {
                newValue = instantiateAndInitialize(actualType);
                if (fieldType.equals(com.fasterxml.jackson.databind.node.ObjectNode.class) && !fieldType.isInstance(newValue)) {
                    newValue = JacksonUtils.getDefaultMapper().valueToTree(newValue);
                }
                field.set(instance, newValue);
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize field {} in class {}: {}", field.getName(), mainClass.getName(), e.getMessage());
        }
    }

    // Collections and maps handling.

    // @SerializeAs annotation handling for containers.
    private void handleContainerField(Field field, Object instance, Object value) {
        SerializeAs serializeAnnotation = field.getAnnotation(SerializeAs.class);
        if (serializeAnnotation != null) {
            handleModifiedContainer(serializeAnnotation, field, instance);
        } else {
            handleNonModifiedContainer(value, field, instance);
        }
    }

    // If annotated, instantiate a new object of the specified type.
    private void handleModifiedContainer(SerializeAs serializeAnnotation, Field field, Object instance) {
        try {
            Class<?> valueType = serializeAnnotation.value();
            if (Collection.class.isAssignableFrom(field.getType())) {
                List<Object> newList = new ArrayList<>();
                newList.add(instantiateAndInitialize(valueType));
                field.set(instance, newList);
            } else if (Map.class.isAssignableFrom(field.getType())) {
                Map<Object, Object> newMap = new HashMap<>();
                newMap.put("key", instantiateAndInitialize(valueType));
                field.set(instance, newMap);
            }
        } catch (Exception e) {
            logger.warn("Failed to instantiate @SerializeAs container for field {}: {}", field.getName(), e.getMessage());
        }
    }

    // If not annotated, check if the value is null and instantiate a new container if needed.
    private void handleNonModifiedContainer(Object value, Field field, Object instance) {
        if (value == null) {
            try {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    value = new ArrayList<>();
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    value = new HashMap<>();
                }
                field.set(instance, value);
            } catch (Exception e) {
                logger.warn("Failed to instantiate container for field {}: {}", field.getName(), e.getMessage());
                return;
            }
        }

        if (Collection.class.isAssignableFrom(field.getType())) {
            populateContainerWithDummyEntry(value, field.getGenericType(), true);
        } else if (Map.class.isAssignableFrom(field.getType())) {
            populateContainerWithDummyEntry(value, field.getGenericType(), false);
        }
    }

    // Object instantiation, initialization and dummy population.

    private void populateContainerWithDummyEntry(Object container, Type genericType, boolean isCollection) {
        if (isCollection) {
            handleCollectionDummyPopulating(container, genericType);
        } else {
            handleMapDummyPopulating(container, genericType);
        }
    }

    // Populate a collection with a dummy entry if it's empty.
    @SuppressWarnings("unchecked")
    private void handleCollectionDummyPopulating(Object container, Type genericType) {
        Collection<?> collection = (Collection<?>) container;
        if (!collection.isEmpty()) {
            return;
        }

        JavaType javaType = JacksonUtils.getDefaultMapper().getTypeFactory().constructType(genericType);
        JavaType elementType = javaType.getContentType();
        if (elementType == null) {
            return;
        }

        Class<?> rawElementType = elementType.getRawClass();
        if (Utils.isPrimitiveOrString(rawElementType) || rawElementType.isEnum()) {
            return;
        }

        try {
            ((Collection<Object>) collection).add(instantiateAndInitialize(rawElementType));
        } catch (Exception e) {
            logger.warn("Failed to instantiate collection element of type {}: {}", rawElementType.getName(), e.getMessage());
        }
    }

    // Populate a map with a dummy entry if it's empty.
    @SuppressWarnings("unchecked")
    private void handleMapDummyPopulating(Object container, Type genericType) {
        Map<?, ?> map = (Map<?, ?>) container;
        if (!map.isEmpty()) {
            return;
        }

        JavaType javaType = JacksonUtils.getDefaultMapper().getTypeFactory().constructType(genericType);
        JavaType keyType = javaType.getKeyType();
        JavaType valueType = javaType.getContentType();
        if (keyType == null || valueType == null) {
            return;
        }

        Class<?> rawKeyType = keyType.getRawClass();
        Class<?> rawValueType = valueType.getRawClass();

        if ((Utils.isPrimitiveOrString(rawKeyType) || rawKeyType.isEnum())
                || (Utils.isPrimitiveOrString(rawValueType) || rawValueType.isEnum())) {
            return;
        }

        try {
            Map<Object, Object> castedMap = (Map<Object, Object>) map;
            castedMap.put("key", instantiateAndInitialize(rawValueType));
        } catch (Exception e) {
            logger.warn("Failed to instantiate map dummy entry for key type {} and value type {}: {}", rawKeyType.getName(), rawValueType.getName(), e.getMessage());
        }
    }

    private Object instantiateAndInitialize(Class<?> type) throws ReflectiveOperationException {
        Object instance = type.getDeclaredConstructor().newInstance();
        initializeFields(instance, type);
        return instance;
    }
}
