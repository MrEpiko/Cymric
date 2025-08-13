package me.mrepiko.cymric.response.data.components;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.elements.components.button.data.ForgedButtonData;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActionButtonData {

    @JsonUnwrapped
    private ForgedButtonData data;

    @JsonUnwrapped
    @Delegate
    private ActionComponentData componentData;

}
