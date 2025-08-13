package me.mrepiko.cymric.elements.components.selectmenus.entityselect.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntitySelectMenuData {

    private EntitySelectMenu.SelectTarget selectTarget;

}
