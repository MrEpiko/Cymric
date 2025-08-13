package me.mrepiko.cymric.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ElementError {
    ON_COOLDOWN(),
    DISABLED(),
    INVALID_ARGS(),
    NOT_IN_REQUIRED_GUILD(),
    NOT_IN_REQUIRED_CHANNEL(),
    NOT_REQUIRED_USER(),
    NO_REQUIRED_ROLES(),
    USER_LACKS_PERMISSIONS(),
    BOT_LACKS_PERMISSIONS(),
    TALK_REQUIRED(),
    USER_NOT_ADMIN();

    public static List<ElementError> getAll() {
        return List.of(values());
    }

    public static List<ElementError> getAllWithout(ElementError... errors) {
        return Stream.of(values())
                .filter(error -> !List.of(errors).contains(error))
                .toList();
    }

}
