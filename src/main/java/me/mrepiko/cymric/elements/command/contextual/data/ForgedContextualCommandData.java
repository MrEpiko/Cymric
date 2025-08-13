package me.mrepiko.cymric.elements.command.contextual.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.elements.containers.ConditionalDataContainer;
import me.mrepiko.cymric.elements.command.data.CommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.elements.data.ConditionalData;
import me.mrepiko.cymric.elements.data.DeferrableElementData;
import me.mrepiko.cymric.elements.data.ElementData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SupportsDefaultOverriding
public class ForgedContextualCommandData implements ConditionalDataContainer {

    @JsonUnwrapped
    @Delegate
    private ContextualCommandData contextualCommandData = new ContextualCommandData();

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
    public List<JdaCommandData> getJdaCommandData(@Nullable PlaceholderMap map) {
        List<String> names = commandData.getAllNames(getName(), map);
        List<JdaCommandData> dataList = new ArrayList<>();

        for (String name : names) {
            dataList.add(getCommandData(name));
        }

        return dataList;
    }

    @NotNull
    private JdaCommandData getCommandData(@NotNull String name) {
        Command.Type contextType = contextualCommandData.getContextType();
        JdaCommandData data = new JdaCommandData(Commands.context(contextType, name));
        commandData.syncCommandData(data, conditionalData);
        return data;
    }

}
