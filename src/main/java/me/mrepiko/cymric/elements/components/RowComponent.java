package me.mrepiko.cymric.elements.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

@AllArgsConstructor
@Getter
public class RowComponent {

    private final ActionComponent actionComponent;
    private final int rowIndex;

}
