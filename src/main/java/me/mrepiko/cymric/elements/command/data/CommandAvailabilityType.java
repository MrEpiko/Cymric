package me.mrepiko.cymric.elements.command.data;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.List;

@Getter
public enum CommandAvailabilityType {
    GLOBAL(InteractionContextType.GUILD, InteractionContextType.BOT_DM),
    GUILD(InteractionContextType.GUILD),
    BOT_DM(InteractionContextType.BOT_DM);

    CommandAvailabilityType(InteractionContextType... interactionContextTypes) {
        this.interactionContextTypes = List.of(interactionContextTypes);
    }

    private final List<InteractionContextType> interactionContextTypes;
}
