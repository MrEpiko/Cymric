package me.mrepiko.cymric.elements.command.chat.data;

import lombok.*;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatCommandOptionData {

    private String name;
    private String description;
    private OptionType optionType = OptionType.STRING;
    private boolean required;
    private boolean autocomplete;
    private List<ChatCommandChoiceData> choices;

    private int minStringLength;
    private int maxStringLength;

    private double minValue;
    private double maxValue;

    @Nullable
    public List<Command.Choice> getAssembledChoices(@Nullable PlaceholderMap map) {
        if (choices == null || choices.isEmpty()) {
            return List.of();
        }
        if (choices.size() == 1) {
            ChatCommandChoiceData first = choices.getFirst();
            if (first.getName() == null || first.getName().isEmpty()) {
                return null;
            }
        }
        return choices == null ? null : choices
                .stream()
                .map(choice -> choice.getAssembled(map))
                .toList();
    }

    @NotNull
    public OptionData getAssembledOptionData(@Nullable PlaceholderMap map) {
        OptionData optionData = new OptionData(
                optionType,
                Utils.applyPlaceholders(map, name),
                Utils.applyPlaceholders(map, description),
                required,
                autocomplete
        );
        List<Command.Choice> assembledChoices = getAssembledChoices(map);
        if (assembledChoices != null && !assembledChoices.isEmpty()) {
            optionData.addChoices(assembledChoices);
        }
        if (minStringLength > 0) {
            optionData.setMinLength(minStringLength);
        }
        if (maxStringLength > 0) {
            optionData.setMaxLength(maxStringLength);
        }
        if (minValue != 0) {
            optionData.setMinValue(minValue);
        }
        if (maxValue != 0) {
            optionData.setMaxValue(maxValue);
        }
        return optionData;
    }

}
