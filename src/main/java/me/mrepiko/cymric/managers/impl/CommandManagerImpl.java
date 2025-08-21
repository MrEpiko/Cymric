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
import me.mrepiko.cymric.elements.command.CommandLoader;
import me.mrepiko.cymric.elements.command.chat.ChatCommandType;
import me.mrepiko.cymric.elements.command.chat.CommandFunctionalityType;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.command.chat.subtypes.ParentChatCommand;
import me.mrepiko.cymric.elements.command.contextual.GenericContextualCommand;
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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.mrepiko.cymric.mics.Utils.applyPlaceholders;

public class CommandManagerImpl extends GenericElementManager<CommandLoader<?>> implements CommandManager {

    private final CymricApi instance = DiscordBot.getInstance();
    private final CymricConfig config = instance.getConfig();

    private final List<String> DIRECTORY_PATHS = List.of(
            Constants.NORMAL_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH,
            Constants.CONTEXTUAL_COMMAND_CONFIGURATION_FOLDER_PATH
    );

    private final List<Class<? extends CommandLoader<?>>> commandTypes = List.of(
            GenericChatCommand.class,
            GenericContextualCommand.class
    );

    public CommandManagerImpl() {
        instance.getShardManager().addEventListener(this);
    }

    // Command Discord-wise registration

    @Override
    public void registerGlobalCommands() {
        if (config.getData().isDevelopment()) {
            return;
        }

        List<JdaCommandData> dataList = new ArrayList<>();
        dataList.addAll(getCommandData(CommandAvailabilityType.GLOBAL, null));
        dataList.addAll(getCommandData(CommandAvailabilityType.BOT_DM, null));
        dataList.addAll(getCommandData(CommandAvailabilityType.GUILD, null));

        JDA jda = instance.getFirstShard();
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

    private boolean shouldIncludeCommand(CommandLoader<?> commandHolder, CommandAvailabilityType type, @Nullable Guild guild) {
        CommandData data = commandHolder.getCommandData();

        if (commandHolder instanceof GenericChatCommand chatCommand) {
            ForgedChatCommandData chatData = chatCommand.getData();
            // Exclude subcommands (only include top-level for registration)
            if (chatData.getParentCommand() != null) {
                return false;
            }
            // Exclude parent commands that have no children
            if (chatCommand.getType() == CommandFunctionalityType.PARENT && (chatData.getChildrenCommands() == null || chatData.getChildrenCommands().isEmpty())) {
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
            CommandLoader<?> holder = getByFullName(
                    command.getFullCommandName(),
                    CommandLoader.class
            );
            if (holder == null) {
                throw new IllegalArgumentException("Command with full name " + command.getFullCommandName() + " not found in registered commands.");
            }
            handleChildrenRegistration(holder, command);
            holder.setDiscordCommand(command);
        }
    }

    // Set Discord command for (grand)children commands
    private void handleChildrenRegistration(@NotNull CommandLoader<?> holder, @NotNull Command discordCommand) {
        if (!(holder instanceof GenericChatCommand chatCommand)) {
            return;
        }
        List<GenericChatCommand> children = chatCommand.getData().getChildrenCommands();
        if (children == null) {
            return;
        }
        for (GenericChatCommand child : children) {
            child.setDiscordCommand(discordCommand);

            List<GenericChatCommand> grandchildren = child.getData().getChildrenCommands();
            if (grandchildren == null || grandchildren.isEmpty()) {
                continue;
            }
            for (GenericChatCommand grandchild : grandchildren) {
                grandchild.setDiscordCommand(discordCommand);
            }
        }
    }

    // Command bot-wise registration

    @Override
    public void register() {
        for (String path : DIRECTORY_PATHS) {
            setupDirectory(path);
        }
        for (Class<? extends CommandLoader<?>> type : commandTypes) {
            register(CymricCommand.class, type);
        }
        formCommandFamilyTree();
    }

    // SubcommandGroup & Subcommand organizing

    // This method organizes commands into a family tree structure (parent, child, grandchild).
    private void formCommandFamilyTree() {
        for (CommandLoader<?> holder : elements.values()) {
            if (!(holder instanceof GenericChatCommand chatCommand)) {
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
                CommandLoader<?> childHolder = getById(childId);
                if (!(childHolder instanceof GenericChatCommand child)) {
                    throw new IllegalArgumentException("Child command with ID " + childId + " is not a GenericChatCommand.");
                }
                if (child == parent) {
                    throw new IllegalArgumentException("A command cannot be its own child: " + parent.getId());
                }

                // At this point, child can also be a parent to other commands.
                parentData.getChildrenCommands().add(child);
                child.getData().setParentCommand(parent);
            }
        }
        validateFamilyTree();
    }

    private void validateFamilyTree() {
        for (CommandLoader<?> holder : elements.values()) {
            if (!(holder instanceof GenericChatCommand parent)) {
                continue;
            }

            List<GenericChatCommand> childrenCommands = parent.getData().getChildrenCommands();
            if (childrenCommands == null || childrenCommands.isEmpty()) {
                if (parent.getType() == CommandFunctionalityType.PARENT) {
                    DiscordBot.getLogger().warn("Command {} is a ParentChatCommand but has no subcommands. This command will not be registered.", parent.getId());
                }
                continue;
            }

            // Ensure that parent command does not have great-grandchildren commands.
            for (GenericChatCommand child : childrenCommands) {
                if (child == parent) {
                    throw new IllegalArgumentException("Command " + parent.getId() + " has a subcommand group " + child.getId() +
                            " which is the same as the parent command.");
                }

                List<GenericChatCommand> grandchildrenCommands = child.getData().getChildrenCommands();
                if (grandchildrenCommands == null || grandchildrenCommands.isEmpty()) {
                    if (child.getType() == CommandFunctionalityType.PARENT) {
                        DiscordBot.getLogger().warn("Command {} has a subcommand group {} which is a ParentChatCommand but has no subcommands. This command will not be registered.", parent.getId(), child.getId());
                    }
                    continue;
                }

                for (GenericChatCommand grandchild : grandchildrenCommands) {
                    String message = getValidationErrorMessage(parent, child, grandchild);

                    List<GenericChatCommand> greatGrandchildrenCommands = grandchild.getData().getChildrenCommands();
                    if (greatGrandchildrenCommands != null && !greatGrandchildrenCommands.isEmpty()) {
                        throw new IllegalArgumentException(message + " that has its own subcommands.");
                    }
                }
            }
        }
    }

    private @NotNull String getValidationErrorMessage(GenericChatCommand parent, GenericChatCommand child, GenericChatCommand grandchild) {
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
    private <T extends CommandLoader<?>> T getByFullName(@NotNull String fullName, @NotNull Class<T> clazz) {
        for (CommandLoader<?> value : elements.values()) {
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
        CommandLoader<?> holder = getByFullName(event.getFullCommandName(), GenericContextualCommand.class);
        if (!(holder instanceof GenericContextualCommand contextualCommand)) {
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
        CommandLoader<?> holder = getByFullName(event.getFullCommandName(), GenericContextualCommand.class);
        if (!(holder instanceof GenericContextualCommand contextualCommand)) {
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
        GenericContextualCommand contextualCommand = context.getCommand();
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
        GenericChatCommand slashCommand = getByFullName(event.getFullCommandName(), GenericChatCommand.class);
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

        GenericChatCommand prefixCommand = payload.genericChatCommand;
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
        GenericChatCommand slashCommand = getByFullName(event.getFullCommandName(), GenericChatCommand.class);
        if (slashCommand == null) {
            return;
        }

        PlaceholderMap map = PlaceholderMapBuilder.create()
                .includeConstantPlaceholders(true)
                .includeCommandPlaceholders(false)
                .includeContextPlaceholders(false)
                .build();

        ForgedChatCommandData data = slashCommand.getData();
        OptionData option = data.getAssembledOption(event.getFocusedOption().getName(), map);
        if (option == null) {
            return;
        }

        event.replyChoices(option.getChoices()
                .stream()
                .filter(x -> x.getName().startsWith(event.getFocusedOption().getValue()))
                .toList()
        ).queue();
    }

    // Other

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
        GenericChatCommand chatCommand = null;
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

            GenericChatCommand temp = getByFullName(fullName, GenericChatCommand.class);
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

        if (data.getChildrenCommands().isEmpty()) {
            return new PrefixCommandPayload(chatCommand, args);
        }

        return new PrefixCommandPayload(getByFullName(fullName, GenericChatCommand.class), args);
    }

    @AllArgsConstructor
    private static class PrefixCommandPayload {
        private final GenericChatCommand genericChatCommand;
        private final List<String> args;
    }

}
