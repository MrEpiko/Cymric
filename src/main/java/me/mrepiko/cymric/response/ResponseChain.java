package me.mrepiko.cymric.response;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ResponseChain extends LinkedList<Action> {

    protected ResponseChain() {
        super();
    }

    protected ResponseChain(@NotNull Action action) {
        super(List.of(action));
    }

    protected ResponseChain(@NotNull List<Action> actionList) {
        super(actionList);
    }

}
