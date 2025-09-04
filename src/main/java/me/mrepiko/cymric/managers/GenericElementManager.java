package me.mrepiko.cymric.managers;

import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.elements.modules.Module;
import me.mrepiko.cymric.elements.plain.BotElement;
import me.mrepiko.cymric.elements.plain.Reloadable;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GenericElementManager<T extends BotElement> extends ListenerAdapter implements ElementManager<T>  {

    // ID: Element
    protected final Map<String, T> elements = new HashMap<>();

    @NotNull
    @Override
    public File setupDirectory(@NotNull String directoryPath) {
        try {
            return Utils.getAndCreateIfNotExists(directoryPath, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(@NotNull Class<? extends Annotation> annotation, @Nullable Class<? extends T> type) {
        Set<Class<?>> classes = CymricApi.reflections.getTypesAnnotatedWith(annotation);
        for (Class<?> clazz : classes) {
            if (type != null && !type.isAssignableFrom(clazz)) {
                continue;
            }

            File folder = getFolder(clazz, annotation);
            if (folder != null) {
                register(folder, (Class<T>) clazz);
                continue;
            }

            if (!isModuleEnabled(clazz, annotation)) {
                continue;
            }

            try {
                T element = (T) clazz.getDeclaredConstructor().newInstance();
                register(element);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to instantiate element class: " + clazz.getName(), e);
            }
        }
    }

    @Override
    public <F extends T> void register(@NotNull File dir, @NotNull Class<F> type) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            String elementId = file.getName().replace(".json", "");
            try {
                F element = type.getDeclaredConstructor(String.class).newInstance(elementId);
                register(element);
            } catch (ReflectiveOperationException e) {
                DiscordBot.getLogger().error("Failed to instantiate element class for ID: {}", elementId, e);
            }
        }
    }

    @Override
    public void register(@NotNull T element) {
        if (elements.containsKey(element.getId())) {
            throw new IllegalArgumentException("An element with ID " + element.getId() + " is already registered.");
        }
        if (element instanceof SerializableBotElement<?> serializableElement) {
            serializableElement.reload();
            if (serializableElement.isConfigMissing()) {
                return;
            }
        }
        elements.put(element.getId(), element);
    }

    @NotNull
    @Override
    public T getById(@NotNull String id) {
        if (elements.containsKey(id)) {
            return elements.get(id);
        }
        throw new IllegalArgumentException("Element with ID " + id + " not found.");
    }

    @NotNull
    @Override
    public T getByClass(@NotNull Class<? extends T> clazz) {
        for (T element : elements.values()) {
            if (clazz.isInstance(element)) {
                return element;
            }
        }
        throw new IllegalArgumentException("Element of class " + clazz.getName() + " not found.");
    }

    @NotNull
    @Override
    public List<T> getRegistered() {
        return List.copyOf(elements.values());
    }

    @Override
    public void reload() {
        for (T value : elements.values()) {
            if (value instanceof Reloadable reloadable) {
                reloadable.reload();
            }
        }
    }

    @Override
    public boolean exists(@NotNull String id) {
        return elements.containsKey(id);
    }

    @Nullable
    private File getFolder(@NotNull Class<?> clazz, @NotNull Class<? extends Annotation> annotationClass) {
        for (Method method : annotationClass.getMethods()) {
            if (!method.getName().equals("folderPath") || method.getParameterCount() != 0 || method.getReturnType() != String.class) {
                continue;
            }
            try {
                Annotation annotation = clazz.getAnnotation(annotationClass);
                if (annotation == null) {
                    return null;
                }
                String folderPath = (String) method.invoke(annotation);
                if (folderPath == null || folderPath.isEmpty()) {
                    return null;
                }
                return Utils.getAndCreateIfNotExists(folderPath, true);
            } catch (ReflectiveOperationException e) {
                DiscordBot.getLogger().error("Failed to get folder path from annotation: {}", annotationClass.getName(), e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private boolean isModuleEnabled(@NotNull Class<?> clazz, @NotNull Class<? extends Annotation> annotationClass) {
        for (Method method : annotationClass.getMethods()) {
            if (!method.getName().equals("moduleId") || method.getParameterCount() != 0 || method.getReturnType() != String.class) {
                continue;
            }
            try {
                Annotation annotation = clazz.getAnnotation(annotationClass);
                if (annotation == null) {
                    return true;
                }
                String moduleId = (String) method.invoke(annotation);
                if (moduleId == null || moduleId.isEmpty()) {
                    return true;
                }
                Module module = DiscordBot.getInstance().getModuleManager().getById(moduleId);
                return module.isEnabled();
            } catch (ReflectiveOperationException e) {
                DiscordBot.getLogger().error("Failed to get folder path from annotation: {}", annotationClass.getName(), e);
            }
        }
        return true;
    }

}
