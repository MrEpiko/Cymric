package me.mrepiko.cymric.response.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * ResponseData class that holds a list of ActionData objects.
 * It can be initialized with a single ActionData object or a list of ActionData objects.
 */
@SupportsDefaultOverriding
@JsonDeserialize(using = ResponseDataDeserializer.class)
public class ResponseData extends LinkedList<ActionData> {

    public ResponseData() {
        super();
    }

    public ResponseData(@NotNull ActionData actionData) {
        super(List.of(actionData));
    }

    public ResponseData(@NotNull ActionData... actionData) {
        super(List.of(actionData));
    }

}
