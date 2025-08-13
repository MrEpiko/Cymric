package me.mrepiko.cymric.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OperationsContainer {

    private Consumer<Message> onMessageSend;
    private Consumer<Message> onMessageEdit;
    private Consumer<Void> onMessageDelete;
    private Consumer<ThreadChannel> onThreadCreate;
    private Consumer<Message> onMessagePin;
    private Consumer<Void> onModalSend;

    private Consumer<Throwable> onException;

    public void onMessageSend(@NotNull Message message) {
        if (onMessageSend != null) {
            onMessageSend.accept(message);
        }
    }

    public void onMessageEdit(@NotNull Message message) {
        if (onMessageEdit != null) {
            onMessageEdit.accept(message);
        }
    }

    public void onMessageDelete() {
        if (onMessageDelete != null) {
            onMessageDelete.accept(null);
        }
    }

    public void onThreadCreate(@NotNull ThreadChannel threadChannel) {
        if (onThreadCreate != null) {
            onThreadCreate.accept(threadChannel);
        }
    }

    public void onMessagePin(@NotNull Message message) {
        if (onMessagePin != null) {
            onMessagePin.accept(message);
        }
    }

    public void onModalSend() {
        if (onModalSend != null) {
            onModalSend.accept(null);
        }
    }

    public boolean onExceptionIfSet(@NotNull Throwable throwable) {
        if (onException != null) {
            onException.accept(throwable);
            return true;
        }
        return false;
    }

}
