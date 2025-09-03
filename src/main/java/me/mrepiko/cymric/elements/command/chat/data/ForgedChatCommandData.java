package me.mrepiko.cymric.elements.command.chat.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.config.main.CymricConfig;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import me.mrepiko.cymric.elements.command.data.CommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.elements.containers.ConditionalDataContainer;
import me.mrepiko.cymric.elements.data.ConditionalData;
import me.mrepiko.cymric.elements.data.DeferrableElementData;
import me.mrepiko.cymric.elements.data.ElementData;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.ListStyle;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.placeholders.PlaceholderMapBuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SupportsDefaultOverriding
public class ForgedChatCommandData implements ConditionalDataContainer {

    @JsonUnwrapped
    @Delegate
    private ChatCommandData chatCommandData = new ChatCommandData();

    @JsonUnwrapped
    @Delegate
    private CommandData commandData = new CommandData();

    @JsonUnwrapped
    @Delegate
    private ConditionalData conditionalData = new ConditionalData();

    @JsonUnwrapped
    @Delegate
    private ElementData elementData = new ElementData();

    @JsonUnwrapped
    @Delegate
    private DeferrableElementData deferrableElementData = new DeferrableElementData();

    @NotNull
    public List<JdaCommandData> getCommandData(@NotNull GenericChatCommand command, @Nullable PlaceholderMap map) {
        List<String> names = commandData.getAllNames(getName(), map);
        List<JdaCommandData> dataList = new ArrayList<>();

        for (String name : names) {
            dataList.add(getCommandData(command, name, map));
        }

        return dataList;
    }

    @NotNull
    private JdaCommandData getCommandData(@NotNull GenericChatCommand command, @NotNull String name, @Nullable PlaceholderMap map) {
        if (name.length() > 32) {
            throw new IllegalArgumentException("Command name '" + name + "' exceeds the maximum length of 32 characters.");
        }
        if (command.getParentCommand() != null) {
            throw new IllegalStateException("Cannot create command data for a child command.");
        }

        JdaCommandData data = new JdaCommandData(
                Commands.slash(
                        name,
                        Utils.applyPlaceholders(map, getDescription())
                )
        );

        List<OptionData> assembledOptions = getAssembledOptions(map);
        if (assembledOptions != null) {
            data.getAsSlashCommandData().addOptions(assembledOptions);
        }

        List<GenericChatCommand> childrenCommands = command.getChildrenCommands();
        if (childrenCommands != null) {
            for (GenericChatCommand child : childrenCommands) {
                List<GenericChatCommand> grandchildren = child.getChildrenCommands();
                if (grandchildren != null && !grandchildren.isEmpty()) { // Parent has grandchildren, so it's a subcommand group.
                    data.getAsSlashCommandData().addSubcommandGroups(
                            getSubcommandGroupData(child, map)
                    );
                    continue;
                }
                // Otherwise, it's a subcommand.
                data.getAsSlashCommandData().addSubcommands(
                        getSubcommandData(child, map)
                );
            }
        }

        commandData.syncCommandData(data, conditionalData);
        return data;
    }

    @Nullable
    public List<OptionData> getAssembledOptions(@Nullable PlaceholderMap map) {
        List<ChatCommandOptionData> options = chatCommandData.getOptions();
        if (options == null) {
            return List.of();
        }
        if (options.size() == 1) {
            ChatCommandOptionData first = options.getFirst();
            if (first.getName() == null || first.getName().isEmpty()) {
                return null;
            }
        }
        return options
                .stream()
                .map(option -> option.getAssembledOptionData(map))
                .toList();
    }

    @Nullable
    public OptionData getAssembledOption(@NotNull String name, @Nullable PlaceholderMap map) {
        List<ChatCommandOptionData> options = chatCommandData.getOptions();
        return options == null ? null : options
                .stream()
                .filter(option -> option.getName().equals(name))
                .findFirst()
                .map(option -> option.getAssembledOptionData(map))
                .orElse(null);
    }

    @NotNull
    private SubcommandGroupData getSubcommandGroupData(@NotNull GenericChatCommand command, @Nullable PlaceholderMap map) {
        ForgedChatCommandData data = command.getData();
        SubcommandGroupData groupData = new SubcommandGroupData(
                Utils.applyPlaceholders(map, data.getName()),
                Utils.applyPlaceholders(map, data.getDescription())
        );
        List<GenericChatCommand> childrenCommands = command.getChildrenCommands();
        if (childrenCommands != null) {
            for (GenericChatCommand child : childrenCommands) {
                groupData.addSubcommands(getSubcommandData(child, map));
            }
        }
        return groupData;
    }

    @NotNull
    public SubcommandData getSubcommandData(@NotNull GenericChatCommand command, @Nullable PlaceholderMap map) {
        ForgedChatCommandData data = command.getData();
        SubcommandData subcommandData = new SubcommandData(
                Utils.applyPlaceholders(map, data.getName()),
                Utils.applyPlaceholders(map, data.getDescription())
        );

        List<OptionData> assembledOptions = data.getAssembledOptions(map);
        if (assembledOptions != null) {
            subcommandData.addOptions(assembledOptions);
        }

        return subcommandData;
    }

    @NotNull
    public String getFullName(@NotNull GenericChatCommand command) {
        String name = elementData.getName();
        GenericChatCommand parent = command.getParentCommand();
        if (parent != null) {
            name = parent.getData().getName() + " " + name;
            GenericChatCommand grandparent = parent.getParentCommand();
            if (grandparent != null) {
                name = grandparent.getData().getName() + " " + name;
            }
        }
        return name;
    }

    @NotNull
    public String getUsage(@NotNull GenericChatCommand command) {
        List<ChatCommandOptionData> options = chatCommandData.getOptions();
        String prefix = DiscordBot.getInstance().getConfig().getPrefix();

        StringBuilder usage = new StringBuilder(prefix).append(getFullName(command));
        for (ChatCommandOptionData option : options) {
            if (option.isRequired()) {
                usage.append(" <").append(option.getName()).append(">");
            } else {
                usage.append(" [").append(option.getName()).append("]");
            }
        }
        return usage.toString();
    }

    @NotNull
    public String getArgExplanation() {
        List<ChatCommandOptionData> options = chatCommandData.getOptions();
        CymricConfig config = DiscordBot.getInstance().getConfig();
        StringBuilder explanation = new StringBuilder();

        PlaceholderMap map = PlaceholderMapBuilder.create().build();
        for (ChatCommandOptionData option : options) {
            map.put("arg_name", option.getName());
            map.put("arg_type", Utils.capitalizeEveryWord(option.getOptionType().name().toLowerCase()));
            map.put("arg_description", option.getDescription());
            map.put("arg_required", (option.isRequired()) ? config.getArgRequired() : config.getArgOptional());
            List<ChatCommandChoiceData> choices = option.getChoices();
            boolean hasChoices = choices != null && !choices.isEmpty() && !option.isAutocomplete();

            if (hasChoices) {
                map.put(
                        "arg_choices",
                        choices.stream().map(ChatCommandChoiceData::getName).toList(),
                        ListStyle.COMMA,
                        "`N/A`"
                );
            }
            explanation.append(map.applyPlaceholders((hasChoices)
                    ? config.getChoiceArgTemplate()
                    : config.getArgTemplate()));
        }
        return explanation.toString();
    }

}
