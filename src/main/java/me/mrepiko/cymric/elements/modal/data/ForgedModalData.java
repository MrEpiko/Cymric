package me.mrepiko.cymric.elements.modal.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.elements.containers.ConditionalDataContainer;
import me.mrepiko.cymric.elements.data.ConditionalData;
import me.mrepiko.cymric.elements.data.DeferrableElementData;
import me.mrepiko.cymric.elements.data.ElementData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SupportsDefaultOverriding
public class ForgedModalData implements ConditionalDataContainer {

    @JsonUnwrapped
    @Delegate
    private ModalData modalData = new ModalData();

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
    public Modal getModal(@NotNull String id, @Nullable PlaceholderMap map) {
        String title = modalData.getTitle();
        List<ModalTextFieldData> textFields = modalData.getTextFields();

        Modal.Builder builder = Modal.create(id, Utils.applyPlaceholders(map, title));
        for (ModalTextFieldData field : textFields) {
            builder.addActionRow(field.getAssembled(map));
        }
        return builder.build();
    }

}
