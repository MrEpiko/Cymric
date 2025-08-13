package me.mrepiko.cymric.response.data.embed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FieldData {

    private String name;
    private String value;
    private boolean inline;
    private boolean blank;

}
