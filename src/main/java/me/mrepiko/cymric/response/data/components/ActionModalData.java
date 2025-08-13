package me.mrepiko.cymric.response.data.components;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActionModalData {

    @JsonUnwrapped
    private ForgedModalData modalData = new ForgedModalData();

    @JsonUnwrapped
    @Delegate
    private ActionComponentData componentData;

}
