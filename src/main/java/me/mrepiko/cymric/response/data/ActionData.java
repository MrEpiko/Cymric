package me.mrepiko.cymric.response.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;
import me.mrepiko.cymric.annotations.SerializeAs;
import me.mrepiko.cymric.response.data.components.*;
import me.mrepiko.cymric.response.data.embed.MessageEmbedData;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActionData {

    private String messageId;
    private String guildId;
    private String channelId;
    private boolean forceMessage;

    private String content;
    private boolean ephemeral;
    private List<String> reactions;
    private String threadName;
    private double delayMillis;

    private boolean reply;
    private boolean edit;
    private boolean delete;
    private boolean pin;
    private boolean sendTyping;

    private boolean clearContent;
    private boolean clearEmbeds;
    private boolean clearFiles;
    private boolean clearReactions;
    private boolean clearComponents;

    private List<MessageEmbedData> embeds;
    private List<String> filePaths;

    // Inherits sent message from the previous action.
    private boolean inheritMessage;

    @SerializeAs(ActionButtonData.class)
    private List<ObjectNode> buttons;
    @SerializeAs(ActionStringSelectMenuData.class)
    private List<ObjectNode> stringSelectMenus;
    @SerializeAs(ActionEntitySelectMenuData.class)
    private List<ObjectNode> entitySelectMenus;

    @SerializeAs(ActionModalData.class)
    private ObjectNode modal;

}
