package me.mrepiko.cymric.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.SneakyThrows;
import me.mrepiko.cymric.jackson.JacksonUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class ConfigFile {

    @Getter
    private final ObjectNode topLevelNode;
    private final File file;
    private final ObjectMapper mapper;

    public ConfigFile(String filePath, boolean createIfNotExists) {
        this(new File(filePath), createIfNotExists);
    }

    public ConfigFile(String filePath) {
        this(new File(filePath), false);
    }

    @SneakyThrows
    public ConfigFile(File file, boolean createIfNotExists) {
        this.file = file;
        if (createIfNotExists) {
            createFileIfNotExists();
        }
        setupFileAsJson();
        this.mapper = JacksonUtils.getDefaultMapper();
        JsonNode node = mapper.readTree(file);
        if (!node.isObject()) {
            throw new IllegalArgumentException("The file must contain a JSON object at the top level.");
        }
        this.topLevelNode = (ObjectNode) node;
    }

    @SneakyThrows
    public void save() {
        try (Writer out = new FileWriter(file, false)) {
            String input = topLevelNode.toString();
            out.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(input)));
        }
    }

    private void createFileIfNotExists() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setupFileAsJson() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create config file: " + file.getAbsolutePath(), e);
            }
        }
        if (file.length() == 0) {
            try (Writer writer = new FileWriter(file)) {
                writer.write("{}");
            } catch (IOException e) {
                throw new RuntimeException("Failed to write to config file: " + file.getAbsolutePath(), e);
            }
        }
    }

}
