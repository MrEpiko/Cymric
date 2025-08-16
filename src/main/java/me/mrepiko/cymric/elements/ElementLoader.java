package me.mrepiko.cymric.elements;

import lombok.Getter;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class ElementLoader<T> implements SerializableBotElement<T> {

    protected final String id;
    protected T data;
    protected JsonContainer config;

    protected boolean configMissing;
    protected final String filePath;

    public ElementLoader(@NotNull String id, @NotNull String folderPath) {
        this.id = id;
        this.filePath = folderPath + id + ".json";
        if (!Utils.isFileExists(this.filePath)) {
            this.configMissing = true;
        }
    }

    @Override
    public void reload() {
        if (this.configMissing) {
            return;
        }
        this.config = new JsonContainer(new ConfigFile(this.filePath));
        initializeData();
        JacksonUtils.mergeDeclaredFieldsFromJson(this, config);
    }

    public abstract void initializeData();

}
