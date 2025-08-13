package me.mrepiko.cymric.elements.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComponentData {

    private int rowIndex;
    private boolean disableOnceUsed;
    private boolean disableAllOnceUsed;
    private boolean creatorOnly;
    private boolean appearAsDisabled;

    public void syncActionComponent(@NotNull ActionComponent actionComponent) {
        if (appearAsDisabled) {
            actionComponent = actionComponent.asDisabled();
        }
    }

}
