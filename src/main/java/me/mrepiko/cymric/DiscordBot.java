package me.mrepiko.cymric;

import lombok.Getter;
import me.mrepiko.cymric.config.defaultobject.DefaultObjectConfig;
import me.mrepiko.cymric.config.main.CymricConfig;
import me.mrepiko.cymric.elements.tasks.GenericTask;
import me.mrepiko.cymric.elements.tasks.cacheable.GenericCacheableTask;
import me.mrepiko.cymric.managers.*;
import me.mrepiko.cymric.managers.impl.*;
import me.mrepiko.cymric.mics.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordBot extends ListenerAdapter implements CymricApi {

    @Getter
    private static CymricApi instance;

    @Getter
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    @Getter
    private ShardManager shardManager;
    @Getter
    private CommandManager commandManager;
    @Getter
    private ComponentManager componentManager;
    @Getter
    private ModalManager modalManager;
    @Getter
    private ModuleManager moduleManager;
    @Getter
    private TaskManager taskManager;

    @Getter
    private CymricConfig config;

    @Getter
    private final DefaultObjectConfig defaultObjectConfig = new DefaultObjectConfig();

    @Getter
    private EventWaiter eventWaiter;

    @NotNull
    @Override
    public JDA getFirstShard() {
        return shardManager.getShards().getFirst();
    }

    public static void main(String[] args) {
        new DiscordBot().start();
    }

    @Override
    public void start() {
        instance = this;

        this.config = new CymricConfig();
        initializeShardManager();
        setupManagersAndElements();
        setupEventWaiter();
        startCommandScanner();
    }

    private void initializeShardManager() {
        DefaultShardManagerBuilder builder = config.getBuilder();
        builder.addEventListeners(this);
        this.shardManager = builder.build();
    }

    private void setupManagersAndElements() {
        this.commandManager = new CommandManagerImpl();
        this.componentManager = new ComponentManagerImpl();
        this.modalManager = new ModalManagerImpl();
        this.moduleManager = new ModuleManagerImpl();
        this.taskManager = new TaskManagerImpl();

        this.commandManager.register();
        this.componentManager.register();
        this.modalManager.register();
        this.moduleManager.register();
        this.taskManager.register();
    }

    private void setupEventWaiter() {
        this.eventWaiter = new EventWaiter();
        this.shardManager.addEventListener(eventWaiter);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.commandManager.registerGlobalCommands();
        this.moduleManager.enableModules();
        this.taskManager.startAllTasks();
        logger.info("Bot is ready, shard count: {}", event.getJDA().getShardInfo().getShardTotal());
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        this.commandManager.registerGuildCommands(event.getGuild());
    }

    @Override
    public void reload() {
        logger.info("Reloading the bot...");
        this.commandManager.reload();
        this.componentManager.reload();
        this.modalManager.reload();
        this.moduleManager.reload();
    }

    @Override
    public void reboot() {
        logger.info("Rebooting the bot...");
        // Run all cacheable tasks to make sure they are synced with database (if present)
        for (GenericTask task : taskManager.getRegistered()) {
            if (!(task instanceof GenericCacheableTask<?,?>)) {
                continue;
            }
            task.stop();
            task.run();
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000L * 10);
    }

    private void startCommandScanner() {
        new Thread(() -> {
            try (Scanner sc = new Scanner(System.in)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    try {
                        String[] args = line.split(" ");
                        String command = args[0];
                        switch (command.toLowerCase()) {
                            case "help":
                                logger.info("Available commands: help, reboot, reload");
                                break;
                            case "reboot":
                                reboot();
                                break;
                            case "reload":
                                reload();
                                break;
                            default:
                                logger.warn("Unknown command: {}", command);
                        }
                    } catch (Exception e) {
                        logger.error("Error while scanning command", e);
                    }
                }
            }
        }).start();
    }

}
