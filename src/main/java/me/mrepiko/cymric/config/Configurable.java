package me.mrepiko.cymric.config;

import me.mrepiko.cymric.jackson.JsonContainer;

public interface Configurable {
    JsonContainer getConfig();
}
