package me.mrepiko.cymric.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import me.mrepiko.cymric.config.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class JsonContainer {

    private final ObjectMapper mapper;

    @Getter
    private final ObjectNode mainNode;

    @Nullable
    @Getter
    private final ConfigFile configFile;

    private JsonContainer(ObjectNode mainNode, @Nullable ConfigFile configFile) {
        this.mainNode = mainNode;
        this.configFile = configFile;
        this.mapper = JacksonUtils.getDefaultMapper();
    }

    public JsonContainer(@NotNull ObjectNode jsonObject) {
        this(jsonObject, null);
    }

    public JsonContainer(@Nullable ConfigFile configFile, @NotNull ObjectNode jsonObject) {
        this(jsonObject, configFile);
    }

    public JsonContainer(@NotNull ConfigFile configFile) {
        this(configFile.getTopLevelNode(), configFile);
    }

    public <T> JsonContainer(@NotNull T object) {
        this(JacksonUtils.getDefaultMapper().valueToTree(object), null);
    }

    public static <T> JsonContainer of(@NotNull T object, @NotNull ObjectMapper mapper) {
        return new JsonContainer(mapper.valueToTree(object), null);
    }

    public static JsonContainer empty() {
        return new JsonContainer(JacksonUtils.createObjectNode(), null);
    }

    private void saveConfig() {
        if (configFile == null) {
            return;
        }
        try {
            configFile.save();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    private void checkIfKeyExists(String key) {
        if (!has(key)) {
            throw new IllegalArgumentException("Key '" + key + "' does not exist in the JSON object.");
        }
    }

    private void checkIfType(String key, Predicate<JsonNode> predicate, String expectedType) {
        if (!predicate.test(mainNode.get(key))) {
            throw new IllegalArgumentException("Key '" + key + "' is not a JSON " + expectedType + ".");
        }
    }

    private void repair(Object defaultValue) {
        ObjectNode defaultNode = mapper.valueToTree(defaultValue);
        defaultNode.fieldNames().forEachRemaining(field -> {
            if (!mainNode.has(field)) {
                mainNode.set(field, defaultNode.get(field));
            }
        });
        saveConfig();
    }

    // Getters for primitive types

    public String getString(String key) {
        checkIfKeyExists(key);
        if (isNull(key)) {
            return null;
        }
        return mainNode.get(key).asText();
    }

    public String getString(String key, String defaultValue) {
        if (!has(key)) {
            if (isNull(key)) {
                return null;
            }
            return defaultValue;
        }
        return mainNode.get(key).asText(defaultValue);
    }

    public String getStringOrSetDefault(String key, String defaultValue) {
        if (has(key)) {
            return mainNode.get(key).asText();
        }
        set(key, defaultValue);
        return defaultValue;
    }

    public int getInt(String key) {
        checkIfKeyExists(key);
        if (isNull(key)) {
            return 0;
        }
        return mainNode.get(key).asInt();
    }

    public int getInt(String key, int defaultValue) {
        if (!has(key)) {
            if (isNull(key)) {
                return 0;
            }
            return defaultValue;
        }
        return mainNode.get(key).asInt(defaultValue);
    }

    public int getIntOrSetDefault(String key, int defaultValue) {
        if (has(key)) {
            return mainNode.get(key).asInt();
        }
        set(key, defaultValue);
        return defaultValue;
    }

    public boolean getBoolean(String key) {
        checkIfKeyExists(key);
        if (isNull(key)) {
            return false;
        }
        return mainNode.get(key).asBoolean();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (!has(key)) {
            if (isNull(key)) {
                return false;
            }
            return defaultValue;
        }
        return mainNode.get(key).asBoolean(defaultValue);
    }

    public boolean getBooleanOrSetDefault(String key, boolean defaultValue) {
        if (has(key)) {
            return mainNode.get(key).asBoolean();
        }
        set(key, defaultValue);
        return defaultValue;
    }

    public double getDouble(String key) {
        checkIfKeyExists(key);
        if (isNull(key)) {
            return 0.0;
        }
        return mainNode.get(key).asDouble();
    }

    public double getDouble(String key, double defaultValue) {
        if (!has(key)) {
            if (isNull(key)) {
                return 0.0;
            }
            return defaultValue;
        }
        return mainNode.get(key).asDouble(defaultValue);
    }

    public double getDoubleOrSetDefault(String key, double defaultValue) {
        if (has(key)) {
            return mainNode.get(key).asDouble();
        }
        set(key, defaultValue);
        return defaultValue;
    }

    public long getLong(String key) {
        checkIfKeyExists(key);
        if (isNull(key)) {
            return 0L;
        }
        return mainNode.get(key).asLong();
    }

    public long getLong(String key, long defaultValue) {
        if (!has(key)) {
            if (isNull(key)) {
                return 0L;
            }
            return defaultValue;
        }
        return mainNode.get(key).asLong(defaultValue);
    }

    public long getLongOrSetDefault(String key, long defaultValue) {
        if (has(key)) {
            return mainNode.get(key).asLong();
        }
        set(key, defaultValue);
        return defaultValue;
    }

    // Generic getter with Class<T>

    @Nullable
    public <T> T get(String key, Class<T> type) {
        checkIfKeyExists(key);
        JsonNode node = mainNode.get(key);
        return node.isNull() ? null : mapper.convertValue(node, type);
    }

    public <T> T get(String key, Class<T> type, T defaultValue) {
        if (!has(key)) {
            return defaultValue;
        }
        JsonNode node = mainNode.get(key);
        return node.isNull() ? null : mapper.convertValue(node, type);
    }

    public <T> T getOrSetDefault(String key, Class<T> type, T defaultValue) {
        return getOrSetDefault(key, type, defaultValue, false);
    }

    public <T> T getOrSetDefault(String key, Class<T> type, T defaultValue, boolean repair) {
        if (has(key)) {
            JsonNode node = mainNode.get(key);
            if (repair) {
                repair(defaultValue);
            }
            return node.isNull() ? defaultValue : mapper.convertValue(node, type);
        }
        set(key, defaultValue);
        return defaultValue;
    }

    // Generic getters for entire JsonContainer

    @NotNull
    public <T> T getAs(Class<T> type) {
        return mapper.convertValue(mainNode, type);
    }

    public <T> T getAs(Class<T> type, T defaultValue) {
        try {
            return mapper.convertValue(mainNode, type);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public <T> T getAsOrSetDefault(Class<T> type, T defaultValue) {
        return getAsOrSetDefault(type, defaultValue, false);
    }

    public <T> T getAsOrSetDefault(Class<T> type, T defaultValue, boolean repair) {
        if (mainNode.isEmpty()) {
            set(mapper.valueToTree(defaultValue));
            return defaultValue;
        }
        if (repair) {
            repair(defaultValue);
        }
        return getAs(type);
    }

    // JsonNode getters

    @NotNull
    public JsonNode getJsonNode(String key) {
        checkIfKeyExists(key);
        return mainNode.get(key);
    }

    public JsonNode getJsonNode(String key, JsonNode defaultValue) {
        return has(key) ? mainNode.get(key) : defaultValue;
    }

    public JsonNode getJsonNodeOrSetDefault(String key, JsonNode defaultValue) {
        if (has(key)) {
            return mainNode.get(key);
        }
        set(key, defaultValue);
        return defaultValue;
    }

    // ArrayNode getters

    @NotNull
    public ArrayNode getArray(String key) {
        checkIfKeyExists(key);
        checkIfType(key, JsonNode::isArray, "array");
        return (ArrayNode) mainNode.get(key);
    }

    public ArrayNode getArray(String key, ArrayNode defaultValue) {
        return has(key) ? getArray(key) : defaultValue;
    }

    public ArrayNode getArrayOrSetDefault(String key, ArrayNode defaultValue) {
        if (has(key)) {
            checkIfType(key, JsonNode::isArray, "array");
            return (ArrayNode) mainNode.get(key);
        }
        set(key, defaultValue);
        return defaultValue;
    }

    // ObjectNode / JsonContainer getters

    @NotNull
    public JsonContainer getJsonContainer(String key) {
        checkIfKeyExists(key);
        checkIfType(key, JsonNode::isObject, "object");
        return new JsonContainer((ObjectNode) mainNode.get(key), configFile);
    }

    public JsonContainer getJsonContainer(String key, JsonContainer defaultValue) {
        return has(key) ? getJsonContainer(key) : defaultValue;
    }

    public JsonContainer getJsonContainerOrSetDefault(String key, JsonContainer defaultValue) {
        if (has(key)) {
            checkIfType(key, JsonNode::isObject, "object");
            return new JsonContainer((ObjectNode) mainNode.get(key), configFile);
        }
        set(key, defaultValue.getMainNode());
        return new JsonContainer(defaultValue.getMainNode(), configFile);
    }

    // List getters with TypeReference

    public <T> List<T> getList(String key, Class<T> elementClass) throws IOException {
        JsonNode node = mainNode.get(key);
        if (node == null || !node.isArray()) {
            throw new IllegalArgumentException("Key '" + key + "' does not exist or is not a JSON array.");
        }
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        return mapper.readValue(mapper.treeAsTokens(node), type);
    }

    public <T> List<T> getList(String key, Class<T> elementClass, List<T> defaultValue) throws IOException {
        JsonNode node = mainNode.get(key);
        if (node == null) {
            return defaultValue;
        }
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        return mapper.readValue(mapper.treeAsTokens(node), type);
    }

    public <T> List<T> getListOrSetDefault(String key, Class<T> elementClass, List<T> defaultValue) throws IOException {
        JsonNode node = mainNode.get(key);
        if (node != null) {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
            if (node.isArray()) {
                return mapper.readValue(mapper.treeAsTokens(node), type);
            } else {
                throw new IllegalArgumentException("Key '" + key + "' is not a JSON array.");
            }
        }
        set(key, defaultValue);
        return defaultValue;
    }

    // List shortcuts

    public List<String> getStringList(String key) throws IOException {
        return getList(key, String.class);
    }

    public List<String> getStringList(String key, List<String> defaultValue) throws IOException {
        return getList(key, String.class, defaultValue);
    }

    public List<String> getStringListOrSetDefault(String key, List<String> defaultValue) throws IOException {
        return getListOrSetDefault(key, String.class, defaultValue);
    }

    public List<Integer> getIntegerList(String key) throws IOException {
        return getList(key, Integer.class);
    }

    public List<Integer> getIntegerList(String key, List<Integer> defaultValue) throws IOException {
        return getList(key, Integer.class, defaultValue);
    }

    public List<Integer> getIntegerListOrSetDefault(String key, List<Integer> defaultValue) throws IOException {
        return getListOrSetDefault(key, Integer.class, defaultValue);
    }

    // Setters

    public void set(String key, Object value) {
        mainNode.set(key, mapper.valueToTree(value));
        saveConfig();
    }

    public void set(String key, JsonNode node) {
        mainNode.set(key, node);
        saveConfig();
    }

    public void set(ObjectNode node) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Provided node must be a non-null JSON object.");
        }
        mainNode.setAll(node);
        saveConfig();
    }

    // Remove key

    public void remove(String key) {
        mainNode.remove(key);
        saveConfig();
    }

    // Check existence and type helpers

    public boolean has(String key) {
        return mainNode.has(key);
    }

    public boolean isNull(String key) {
        return !has(key) || mainNode.get(key).isNull();
    }

    public boolean hasAndIsNotNull(String key) {
        return !isNull(key);
    }

    public boolean isObject(String key) {
        return has(key) && mainNode.get(key).isObject();
    }

    public boolean isArray(String key) {
        return has(key) && mainNode.get(key).isArray();
    }

    public boolean isEmpty() {
        return mainNode.isEmpty();
    }

    // Expect methods

    public void expect(String key, JsonContainer jsonContainer) {
        JsonNode node = jsonContainer.getMainNode();
        if (has(key)) {
            return;
        }
        set(key, node);
    }

    public void expect(String key, Object value) {
        JsonNode node = mapper.valueToTree(value);
        if (has(key)) {
            return;
        }
        set(key, node);
    }

    public void expect(Object value) {
        ObjectNode updateNode = mapper.valueToTree(value);
        applyDefaults(mainNode, updateNode);
    }

    public void expect(JsonContainer jsonContainer) {
        ObjectNode updateNode = jsonContainer.getMainNode();
        applyDefaults(mainNode, updateNode);
    }

    private void applyDefaults(ObjectNode target, ObjectNode defaults) {
        for (Iterator<String> it = defaults.fieldNames(); it.hasNext(); ) {
            String field = it.next();
            JsonNode defaultValue = defaults.get(field);
            if (target.has(field)) {
                continue;
            }
            target.set(field, defaultValue);
        }
        saveConfig();
    }

    // Other

    @Override
    public String toString() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mainNode);
        } catch (Exception e) {
            return mainNode.toString();
        }
    }

    public int getSize() {
        return mainNode.size();
    }

}
