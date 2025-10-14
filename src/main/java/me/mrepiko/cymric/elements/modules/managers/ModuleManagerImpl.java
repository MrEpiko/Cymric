package me.mrepiko.cymric.elements.modules.managers;

import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.elements.CymricModule;
import me.mrepiko.cymric.elements.modules.Module;
import me.mrepiko.cymric.elements.managers.GenericElementManager;
import me.mrepiko.cymric.mics.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ModuleManagerImpl extends GenericElementManager<Module> implements ModuleManager {

    private final CymricApi instance = DiscordBot.getInstance();

    @Override
    public void register() {
        setupDirectory(Constants.MODULE_CONFIGURATION_FOLDER_PATH);
        register(CymricModule.class, Module.class);
    }

    @Override
    public void register(@NotNull Module element) {
        Map<String, Boolean> moduleStatuses = instance.getConfig().getModules();
        if (elements.containsKey(element.getId())) {
            throw new IllegalArgumentException("A module with ID " + element.getId() + " is already registered.");
        }

        element.setEnabled(moduleStatuses.getOrDefault(element.getId(), false));
        if (!element.isEnabled()) {
            return;
        }

        element.reload();
        elements.put(element.getId(), element);
        DiscordBot.getLogger().info("Registered module: {}", element.getId());
    }

    @Override
    public void enableModules() {
        for (Module value : elements.values()) {
            if (!value.isEnabled()) {
                continue;
            }
            value.onEnable();
        }
    }
}
