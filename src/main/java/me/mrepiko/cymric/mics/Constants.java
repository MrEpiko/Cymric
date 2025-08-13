package me.mrepiko.cymric.mics;

public class Constants {

    public static final int HOURS_IN_DAY = 24;
    public static final int MINUTES_IN_DAY = 1440;
    public static final int SECONDS_IN_DAY = 86400;

    public static final int MINUTES_IN_HOUR = 60;
    public static final int SECONDS_IN_HOUR = 3600;

    public static final int SECONDS_IN_MINUTE = 60;

    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    public static final String CONFIGURATION_FOLDER_PATH = "configuration/";

    public static final String MAIN_CONFIG_FILE_PATH = CONFIGURATION_FOLDER_PATH + "config.json";
    public static final String DEFAULT_OBJECTS_FILE_PATH = CONFIGURATION_FOLDER_PATH + "default_objects.json";

    public static final String ERROR_RESPONSES_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "error_responses/";

    public static final String ALL_COMMANDS_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "commands/";
    public static final String CONTEXTUAL_COMMAND_CONFIGURATION_FOLDER_PATH = ALL_COMMANDS_CONFIGURATION_FOLDER_PATH + "contextual/";
    public static final String CHAT_COMMAND_CONFIGURATION_FOLDER_PATH = ALL_COMMANDS_CONFIGURATION_FOLDER_PATH + "chat/";

    public static final String NORMAL_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH = CHAT_COMMAND_CONFIGURATION_FOLDER_PATH + "normal/";
    public static final String RESPONSE_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH = CHAT_COMMAND_CONFIGURATION_FOLDER_PATH + "response/";
    public static final String PARENT_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH = CHAT_COMMAND_CONFIGURATION_FOLDER_PATH + "parents/";

    public static final String COMPONENT_CONFIGURATION_FOLDER_PATH =CONFIGURATION_FOLDER_PATH + "components/";
    public static final String BUTTON_CONFIGURATION_FOLDER_PATH = COMPONENT_CONFIGURATION_FOLDER_PATH + "buttons/";
    public static final String SELECT_MENUS_CONFIGURATION_FOLDER_PATH = COMPONENT_CONFIGURATION_FOLDER_PATH + "selectmenus/";
    public static final String STRING_SELECT_MENU_CONFIGURATION_FOLDER_PATH = SELECT_MENUS_CONFIGURATION_FOLDER_PATH + "string/";
    public static final String ENTITY_SELECT_MENU_CONFIGURATION_FOLDER_PATH = SELECT_MENUS_CONFIGURATION_FOLDER_PATH + "entity/";

    public static final String MODAL_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "modals/";

    public static final String MODULE_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "modules/";

}
