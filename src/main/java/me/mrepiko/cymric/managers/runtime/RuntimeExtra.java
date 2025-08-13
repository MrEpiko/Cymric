package me.mrepiko.cymric.managers.runtime;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeExtra {

    private final Map<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        map.put(key, value);
    }

    // Primitive types

    @Nullable
    public String getString(String key) {
        return (String) map.get(key);
    }

    @Nullable
    public String getString(String key, String defaultValue) {
        return (String) map.getOrDefault(key, defaultValue);
    }

    @Nullable
    public Integer getInteger(String key) {
        return (Integer) map.get(key);
    }

    @Nullable
    public Integer getInteger(String key, Integer defaultValue) {
        return (Integer) map.getOrDefault(key, defaultValue);
    }

    @Nullable
    public Double getDouble(String key) {
        return (Double) map.get(key);
    }

    @Nullable
    public Double getDouble(String key, Double defaultValue) {
        return (Double) map.getOrDefault(key, defaultValue);
    }

    @Nullable
    public Long getLong(String key) {
        return (Long) map.get(key);
    }

    @Nullable
    public Long getLong(String key, Long defaultValue) {
        return (Long) map.getOrDefault(key, defaultValue);
    }

    @Nullable
    public Boolean getBoolean(String key) {
        return (Boolean) map.get(key);
    }

    @Nullable
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return (Boolean) map.getOrDefault(key, defaultValue);
    }

    // Object types

    @Nullable
    public Object get(String key) {
        return map.get(key);
    }

    @Nullable
    public <T> T get(String key, Class<T> type) {
        return type.cast(map.get(key));
    }

    @Nullable
    public <T> T get(String key, Class<T> type, T defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return type.cast(value);
    }

    // Lists

    @Nullable
    public List<?> getList(String key) {
        Object value = map.get(key);
        if (value instanceof List<?>) {
            return (List<?>) value;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a List.");
    }

    @Nullable
    public List<?> getList(String key, List<?> defaultValue) {
        Object value = map.get(key);
        if (value instanceof Iterable) {
            return (List<?>) value;
        }
        return defaultValue;
    }

    // Other

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public Map<String, Object> getMap() {
        return new HashMap<>(map);
    }

    public void putAll(RuntimeExtra other) {
        if (other != null) {
            this.map.putAll(other.getMap());
        }
    }

}
