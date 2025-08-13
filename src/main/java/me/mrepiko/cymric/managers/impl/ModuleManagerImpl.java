package me.mrepiko.cymric.managers.impl;

import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.elements.CymricModal;
import me.mrepiko.cymric.annotations.elements.CymricModule;
import me.mrepiko.cymric.elements.modules.GenericModule;
import me.mrepiko.cymric.managers.GenericElementManager;
import me.mrepiko.cymric.managers.ModuleManager;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ModuleManagerImpl extends GenericElementManager<GenericModule> implements ModuleManager {

    private final CymricApi instance = DiscordBot.getInstance();

    @Override
    public void register() {
        setupDirectory(Constants.MODULE_CONFIGURATION_FOLDER_PATH);
        register(CymricModule.class, GenericModule.class);
    }

    @Override
    public void register(@NotNull GenericModule element) {
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
        for (GenericModule value : elements.values()) {
            if (!value.isEnabled()) {
                continue;
            }
            value.onEnable();
        }
    }
}
