package me.mrepiko.cymric.elements.data;

import lombok.*;
import me.mrepiko.cymric.response.data.ResponseData;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeoutableElementData {

    private double timeoutMillis;
    private ResponseData timeoutResponseData;
    private boolean disableAllOnceTimedOut;
    private boolean disableOnceTimedOut;
    private double timeoutDeletionIntervalMillis;

}
