package me.mrepiko.cymric.elements.components.managers;

import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.managers.ElementManager;
import me.mrepiko.cymric.elements.components.managers.runtime.RuntimeComponent;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ComponentManager extends ElementManager<ComponentHandler<?>> {
    void addRuntimeComponent(@NotNull RuntimeComponent runtimeComponent);

    @Nullable
    RuntimeComponent getRuntimeComponent(@NotNull String uniqueElementId);

    @NotNull
    List<RuntimeComponent> getRuntimeComponents(@NotNull Message message);
}
