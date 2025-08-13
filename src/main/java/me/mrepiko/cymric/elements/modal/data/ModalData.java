package me.mrepiko.cymric.elements.modal.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModalData {

    private String title;
    private List<ModalTextFieldData> textFields;

}
