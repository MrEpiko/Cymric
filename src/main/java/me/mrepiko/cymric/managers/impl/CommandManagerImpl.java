package me.mrepiko.cymric.managers.impl;

import lombok.AllArgsConstructor;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.elements.CymricCommand;
import me.mrepiko.cymric.config.main.CymricConfig;
import me.mrepiko.cymric.context.commands.ContextualCommandContext;
import me.mrepiko.cymric.context.commands.PrefixCommandContext;
import me.mrepiko.cymric.context.commands.SlashCommandContext;
import me.mrepiko.cymric.context.commands.impl.ContextualCommandContextImpl;
import me.mrepiko.cymric.context.commands.impl.PrefixCommandContextImpl;
import me.mrepiko.cymric.context.commands.impl.SlashCommandContextImpl;
import me.mrepiko.cymric.discord.DiscordCache;
import me.mrepiko.cymric.elements.DeferType;
import me.mrepiko.cymric.elements.command.CommandHandler;
import me.mrepiko.cymric.elements.command.chat.ChatCommandHandler;
import me.mrepiko.cymric.elements.command.chat.ChatCommandType;
import me.mrepiko.cymric.elements.command.chat.CommandFunctionalityType;
import me.mrepiko.cymric.elements.command.chat.data.ChatCommandOptionData;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.command.chat.subtypes.ParentChatCommand;
import me.mrepiko.cymric.elements.command.contextual.ContextualCommandHandler;
import me.mrepiko.cymric.elements.command.contextual.data.ForgedContextualCommandData;
import me.mrepiko.cymric.elements.command.data.CommandAvailabilityType;
import me.mrepiko.cymric.elements.command.data.CommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.managers.CommandManager;
import me.mrepiko.cymric.managers.GenericElementManager;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.placeholders.PlaceholderMapBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static me.mrepiko.cymric.mics.Utils.applyPlaceholders;


public class CommandManagerImpl extends GenericElementManager<CommandHandler<?>> implements CommandManager {

    private final CymricApi instance = DiscordBot.getInstance();
    private final Logger logger = DiscordBot.getLogger();
    private final CymricConfig config = instance.getConfig();

    private final List<String> DIRECTORY_PATHS = List.of(
            Constants.NORMAL_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH,
            Constants.CONTEXTUAL_COMMAND_CONFIGURATION_FOLDER_PATH
    );

    private final List<Class<? extends CommandHandler<?>>> commandTypes = List.of(
            ChatCommandHandler.class,
            ContextualCommandHandler.class
    );

    public CommandManagerImpl() {
        instance.getShardManager().addEventListener(this);
    }

    // Command Discord-wise registration

    @Override
    public void registerGlobalCommands() {
        JDA jda = instance.getFirstShard();
        if (config.getData().isDevelopment()) {
            jda.updateCommands().addCommands(List.of()).queue();
            return;
        }

        List<JdaCommandData> dataList = new ArrayList<>();
        dataList.addAll(getCommandData(CommandAvailabilityType.GLOBAL, null));
        dataList.addAll(getCommandData(CommandAvailabilityType.BOT_DM, null));
        dataList.addAll(getCommandData(CommandAvailabilityType.GUILD, null));

        jda.updateCommands().addCommands(
                dataList.stream()
                        .map(JdaCommandData::get)
                        .toList()
        ).queue(this::handlePostRegistration);
    }

    @Override
    public void registerGuildCommands(@NotNull Guild guild) {
        if (config.getData().isDevelopment() && !guild.getId().equalsIgnoreCase(config.getData().getDevelopmentGuildId())) {
            return;
        }
        List<JdaCommandData> dataList = getCommandData(CommandAvailabilityType.GUILD, guild);
        guild.updateCommands().addCommands(
                dataList.stream()
                        .map(JdaCommandData::get)
                        .toList()
        ).queue(this::handlePostRegistration);
    }

    private List<JdaCommandData> getCommandData(CommandAvailabilityType type, @Nullable Guild guild) {
        PlaceholderMap map = PlaceholderMapBuilder.create()
                .includeCommandPlaceholders(false)
                .includeContextPlaceholders(false)
                .includeConstantPlaceholders(true)
                .build();

        return elements.values()
                .stream()
                .filter(commandHolder -> shouldIncludeCommand(commandHolder, type, guild))
                .flatMap(command -> command.getJdaCommandData(map).stream())
                .toList();
    }

    private boolean shouldIncludeCommand(@NotNull CommandHandler<?> handler, CommandAvailabilityType type, @Nullable Guild guild) {
        CommandData data = handler.getCommandData();

        if (handler instanceof ChatCommandHandler chatCommand) {
            ForgedChatCommandData chatData = chatCommand.getData();
            // Exclude subcommands (only include top-level for registration)
            if (chatCommand.getParentCommand() != null) {
                return false;
            }
            // Exclude parent commands that have no children
            if (chatCommand.getType() == CommandFunctionalityType.PARENT && (chatCommand.getChildrenCommands() == null || chatCommand.getChildrenCommands().isEmpty())) {
                return false;
            }
            if (chatData.getCommandType() == ChatCommandType.PREFIX) {
                return false;
            }
        }

        List<String> registeredGuilds = data.getRegisteredGuildIds();
        applyPlaceholders(
                PlaceholderMapBuilder.create()
                        .includeContextPlaceholders(false)
                        .includeCommandPlaceholders(false)
                        .build(),
                registeredGuilds
        );

        if (config.isDevelopment()) {
            return type == CommandAvailabilityType.GUILD;
        }

        if (type == CommandAvailabilityType.GUILD) {
            boolean isGuildScoped = data.getAvailabilityType() == CommandAvailabilityType.GUILD;
            if (guild == null) {
                return isGuildScoped && (registeredGuilds == null || registeredGuilds.isEmpty());
            }
            return registeredGuilds != null && registeredGuilds.contains(guild.getId());
        }

        return type == data.getAvailabilityType();
    }

    private void handlePostRegistration(List<Command> commands) {
        for (Command command : commands) {
            CommandHandler<?> handler = getByFullName(
                    command.getFullCommandName(),
                    CommandHandler.class
            );
            if (handler == null) {
                throw new IllegalArgumentException("Command with full name " + command.getFullCommandName() + " not found in registered commands.");
            }
            handleChildrenRegistration(handler, command);
            handler.setDiscordCommand(command);
        }
    }

    // Set Discord command for (grand)children commands
    private void handleChildrenRegistration(@NotNull CommandHandler<?> handler, @NotNull Command discordCommand) {
        if (!(handler instanceof ChatCommandHandler chatCommand)) {
            return;
        }
        List<ChatCommandHandler> children = chatCommand.getChildrenCommands();
        List<Command.SubcommandGroup> subcommandGroups = discordCommand.getSubcommandGroups();
        List<Command.Subcommand> subcommands = discordCommand.getSubcommands();
        if (children == null || children.isEmpty() || (subcommandGroups.isEmpty() && subcommands.isEmpty())) {
            return;
        }

        if (subcommands.isEmpty()) {
            for (Command.SubcommandGroup group : subcommandGroups) {
                List<Command.Subcommand> groupSubcommands = group.getSubcommands();
                setSubcommandHandlers(groupSubcommands);
            }
            return;
        }

        setSubcommandHandlers(subcommands);
    }

    private void setSubcommandHandlers(@NotNull List<Command.Subcommand> subcommands) {
        for (Command.Subcommand subcommand : subcommands) {
            String name = subcommand.getFullCommandName();
            ChatCommandHandler subHandler = getByFullName(name, ChatCommandHandler.class);
            if (subHandler == null) {
                logger.warn("Subcommand with name {} not found", name);
                continue;
            }
            subHandler.setDiscordCommand(subcommand);
        }
    }

    // Command bot-wise registration

    @Override
    public void register() {
        for (String path : DIRECTORY_PATHS) {
            setupDirectory(path);
        }
        for (Class<? extends CommandHandler<?>> type : commandTypes) {
            register(CymricCommand.class, type);
        }
        formCommandFamilyTree();
    }

    // SubcommandGroup & Subcommand organizing

    // This method organizes commands into a family tree structure (parent, child, grandchild).
    private void formCommandFamilyTree() {
        for (CommandHandler<?> handler : elements.values()) {
            if (!(handler instanceof ChatCommandHandler chatCommand)) {
                continue;
            }
            ForgedChatCommandData parentData = chatCommand.getData();
            List<String> childrenIds = parentData.getChildrenIds();

            if (childrenIds == null || childrenIds.isEmpty()) {
                continue;
            }
            if (!(chatCommand instanceof ParentChatCommand parent)) {
                throw new IllegalArgumentException("Command with ID " + chatCommand.getId() + " has children IDs but is not a ParentChatCommand.");
            }

            for (String childId : childrenIds) {
                CommandHandler<?> childHolder = getById(childId);
                if (!(childHolder instanceof ChatCommandHandler child)) {
                    throw new IllegalArgumentException("Child command with ID " + childId + " is not a ChatCommandHandler.");
                }
                if (child == parent) {
                    throw new IllegalArgumentException("A command cannot be its own child: " + parent.getId());
                }

                // At this point, child can also be a parent to other commands.
                List<ChatCommandHandler> childrenCommands = chatCommand.getChildrenCommands();
                if (childrenCommands == null) {
                    childrenCommands = new ArrayList<>();
                    chatCommand.setChildrenCommands(childrenCommands);
                }

                childrenCommands.add(child);
                child.setParentCommand(parent);
            }
        }
        validateFamilyTree();
    }

    private void validateFamilyTree() {
        for (CommandHandler<?> handler : elements.values()) {
            if (!(handler instanceof ChatCommandHandler parent)) {
                continue;
            }

            List<ChatCommandHandler> childrenCommands = parent.getChildrenCommands();
            if (childrenCommands == null || childrenCommands.isEmpty()) {
                if (parent.getType() == CommandFunctionalityType.PARENT) {
                    logger.warn("Command {} is a ParentChatCommand but has no subcommands. This command will not be registered.", parent.getId());
                }
                continue;
            }

            // Ensure that parent command does not have great-grandchildren commands.
            for (ChatCommandHandler child : childrenCommands) {
                if (child == parent) {
                    throw new IllegalArgumentException("Command " + parent.getId() + " has a subcommand group " + child.getId() +
                            " which is the same as the parent command.");
                }

                List<ChatCommandHandler> grandchildrenCommands = child.getChildrenCommands();
                if (grandchildrenCommands == null || grandchildrenCommands.isEmpty()) {
                    if (child.getType() == CommandFunctionalityType.PARENT) {
                        logger.warn("Command {} has a subcommand group {} which is a ParentChatCommand but has no subcommands. This command will not be registered.", parent.getId(), child.getId());
                    }
                    continue;
                }

                for (ChatCommandHandler grandchild : grandchildrenCommands) {
                    String message = getValidationErrorMessage(parent, child, grandchild);

                    List<ChatCommandHandler> greatGrandchildrenCommands = grandchild.getChildrenCommands();
                    if (greatGrandchildrenCommands != null && !greatGrandchildrenCommands.isEmpty()) {
                        throw new IllegalArgumentException(message + " that has its own subcommands.");
                    }
                }
            }
        }
    }

    private @NotNull String getValidationErrorMessage(@NotNull ChatCommandHandler parent, @NotNull ChatCommandHandler child, @NotNull ChatCommandHandler grandchild) {
        String message = "Command " + parent.getId() + " has a subcommand group " + child.getId() +
                " which has a subcommand " + grandchild.getId();

        if (grandchild.getType() == CommandFunctionalityType.PARENT) {
            throw new IllegalArgumentException(message + " that is a ParentChatCommand.");
        }

        if (grandchild == parent) {
            throw new IllegalArgumentException(message + " that is the same as the parent command.");
        }

        if (grandchild == child) {
            throw new IllegalArgumentException(message + " that is the same as the child command.");
        }
        return message;
    }

    // Getters

    @Nullable
    private <T extends CommandHandler<?>> T getByFullName(@NotNull String fullName, @NotNull Class<T> clazz) {
        for (CommandHandler<?> value : elements.values()) {
            if (!clazz.isInstance(value)) {
                continue;
            }
            if (value.getFullName().equalsIgnoreCase(fullName)) {
                return clazz.cast(value);
            }
        }
        return null;
    }

    // Listeners

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        ContextualCommandHandler contextualCommand = getByFullName(event.getFullCommandName(), ContextualCommandHandler.class);
        if (contextualCommand == null) {
            return;
        }

        Message selectedMessage = event.getTarget();
        DiscordCache.cacheMessage(selectedMessage);
        ContextualCommandContext context = new ContextualCommandContextImpl(
                event,
                contextualCommand,
                selectedMessage,
                null,
                null
        );
        handleContextualCommand(context);
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        ContextualCommandHandler contextualCommand = getByFullName(event.getFullCommandName(), ContextualCommandHandler.class);
        if (contextualCommand == null) {
            return;
        }

        ContextualCommandContext context = new ContextualCommandContextImpl(
                event,
                contextualCommand,
                null,
                event.getTarget(),
                event.getTargetMember()
        );
        handleContextualCommand(context);
    }

    private void handleContextualCommand(@NotNull ContextualCommandContext context) {
        ContextualCommandHandler contextualCommand = context.getCommand();
        GenericContextInteractionEvent<?> event = context.getEvent();
        ForgedContextualCommandData data = contextualCommand.getData();

        if (!contextualCommand.check(context)) {
            return;
        }

        handleDeferring(event.getInteraction(), data.getDeferType());
        contextualCommand.onInteraction(context);
        contextualCommand.setUserCooldown(event.getUser());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        ChatCommandHandler slashCommand = getByFullName(event.getFullCommandName(), ChatCommandHandler.class);
        if (slashCommand == null) {
            return;
        }

        ForgedChatCommandData data = slashCommand.getData();
        SlashCommandContext context = new SlashCommandContextImpl(event, slashCommand);

        if (data.getCommandType() == ChatCommandType.PREFIX || !slashCommand.check(context)) {
            return;
        }

        handleDeferring(event.getInteraction(), data.getDeferType());
        slashCommand.onInteraction(context);
        slashCommand.setUserCooldown(event.getUser());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        PrefixCommandPayload payload = getPrefixCommandPayload(event);
        if (payload == null) {
            return;
        }

        ChatCommandHandler prefixCommand = payload.chatCommand;
        List<String> args = payload.args;

        PrefixCommandContext context = new PrefixCommandContextImpl(prefixCommand, event, args);
        if (!prefixCommand.check(context)) {
            return;
        }

        prefixCommand.onInteraction(context);
        prefixCommand.setUserCooldown(event.getAuthor());
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        ChatCommandHandler slashCommand = getByFullName(event.getFullCommandName(), ChatCommandHandler.class);
        if (slashCommand == null) {
            return;
        }

        PlaceholderMap map = PlaceholderMapBuilder.create()
                .includeConstantPlaceholders(true)
                .includeCommandPlaceholders(false)
                .includeContextPlaceholders(false)
                .build();

        ForgedChatCommandData data = slashCommand.getData();
        ChatCommandOptionData optionData = null;
        for (ChatCommandOptionData option : data.getOptions()) {
            if (!option.getName().equalsIgnoreCase(event.getFocusedOption().getName())) {
                continue;
            }
            optionData = option;
        }
        if (optionData == null || !optionData.isAutocomplete() ) {
            return;
        }
        List<Command.Choice> assembledChoices = optionData.getAssembledChoices(map);
        if (assembledChoices == null || assembledChoices.isEmpty()) {
            return;
        }

        event.replyChoices(assembledChoices
                .stream()
                .filter(x -> x.getName().startsWith(event.getFocusedOption().getValue()))
                .toList()
        ).queue();
    }

    // Other

    @Override
    public void reload() {
        super.reload();
        formCommandFamilyTree();
        validateFamilyTree();
    }

    private void handleDeferring(@NotNull CommandInteraction interaction, @NotNull DeferType deferType) {
        switch (deferType) {
            case ENDURING, EDIT -> interaction.deferReply().queue();
            case EPHEMERAL -> interaction.deferReply(true).queue();
        }
    }

    @Nullable
    private PrefixCommandPayload getPrefixCommandPayload(MessageReceivedEvent event) {
        String contentRaw = event.getMessage().getContentRaw();
        if (!contentRaw.startsWith(config.getPrefix())) {
            return null;
        }

        String[] split = contentRaw.substring(config.getPrefix().length()).split(" ");
        if (split.length == 0) {
            return null;
        }

        String fullName = "";
        boolean commandFound = false;
        ChatCommandHandler chatCommand = null;
        List<String> args = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            if (commandFound) {
                args.add(split[i]);
                continue; // Command has been already found, so no need to check further.
            }

            boolean first = (i == 0);
            if (first) {
                fullName = split[i];
            } else {
                fullName += " " + split[i];
            }

            ChatCommandHandler temp = getByFullName(fullName, ChatCommandHandler.class);
            if (first && temp == null) { // If the first part of the command is not found, return null.
                return null;
            } else if (!first && temp == null) { // If temp is not found, that means that main command has been found.
                commandFound = true;
                args.add(split[i]);
                continue;
            }
            chatCommand = temp;
        }

        if (chatCommand.getType() == CommandFunctionalityType.PARENT) {
            return null; // Returns as ParentChatCommand has no functionality.
        }

        ForgedChatCommandData data = chatCommand.getData();
        if (data.getCommandType() == ChatCommandType.SLASH) {
            return null;
        }

        List<ChatCommandHandler> childrenCommands = chatCommand.getChildrenCommands();
        if (childrenCommands == null || childrenCommands.isEmpty()) {
            return new PrefixCommandPayload(chatCommand, args);
        }

        return new PrefixCommandPayload(getByFullName(fullName, ChatCommandHandler.class), args);
    }

    @AllArgsConstructor
    private static class PrefixCommandPayload {
        private final ChatCommandHandler chatCommand;
        private final List<String> args;
    }

}