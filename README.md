# Cymric

Create powerful Discord bots with ease using Cymric, a JDA wrapper providing efficient and intuitive abstractions for Discord's API. Cymric's main focus is on easily configurable and reusable elements, allowing you to build bots that are both powerful and easy to maintain.

Please keep in mind that this README does not include every single feature of the project. For more information, please refer to the source code or contact the project's author.

## Table of Contents
1. [Getting started](#getting-started)
2. [Main configuration file](#main-configuration-file)
3. [Elements](#elements)
   - [Specific element configuration](#specific-element-configuration)
   - [Default data object configuration](#default-data-object-configuration)
   - [Chat command types](#chat-command-types)
   - [Element error responses](#element-error-responses)
4. [Placeholders](#placeholders)
5. [Responses](#responses)
   - [Sending a message](#sending-a-message)
   - [Message actions](#message-actions)
   - [Message inheriting](#message-inheriting)
6. [EventWaiter](#eventwaiter)
7. [CLI commands](#cli-commands)
9. [License](#license)

## Getting started
1. Clone the `Cymric` repository to your local machine.
2. Customize your bot's behavior.
3. Build and run your bot using Java and the JDA library.

## Main configuration file
Main configuration file aka `config.json` contains some basic information about your bot and its behavior. All fields within here are pretty much self-explanatory, but here's a quick overview of fields that may not be so straightforward:

- `endpoints` - this is a list of endpoints that are used by the bot. These can be used to define custom endpoints for your bot, such as API endpoints, webhooks, etc. For example:
```json
{
   "endpoints" : {
      "name" : {
         "endpoint_url" : "https://showcase.com/",
         "authorization_header" : "Authorization",
         "authorization_token" : "12345"
      }
   }
}
```
- `constants` - this is a list of constants that are used by the bot. These constants are by default appended to every `PlaceholderMap` and are prefixed with `c_`. For example:
```json
{
   "constants" : {
      "owner_role_id": "1234"
   }
}
```
```json
{
   "required_role_ids": ["{c_owner_role_id}"]
}
```
- `arg` fields - these fields are used as template fields for prefix chat commands. They define how will the bot display command's argument explanations.

## Elements
Elements (commands, buttons, modals, etc.) represent the building blocks of your bot. Considering Cymric is all about JSON configuration, these elements can be easily configured using JSON files. Each element has its own data class (also referred to as a "forged data") that defines its properties and behavior. Forged data classes can include common classes such as data classes representing components, timeout-related behavior, conditional behavior, and more.

Each element has its own generic class that defines its type and behavior. In order to register an element (add it to bot's managers), it must be annotated with the corresponding annotation.

| Name                               | Generic class            | Annotation       |
|------------------------------------|--------------------------|------------------|
| Chat command (slash, prefix)       | GenericChatCommand       | @CymricCommand   |
| Contextual command (user, message) | GenericContextualCommand | @CymricCommand   |
| Button                             | GenericButton            | @CymricComponent |
| String select menu                 | GenericStringSelectMenu  | @CymricComponent |
| Entity select menu                 | GenericEntitySelectMenu  | @CymricComponent |
| Modal                              | GenericModal             | @CymricModal     |
| Module                             | GenericModule            | @CymricModule    |
| Task                               | GenericTask              | @CymricTask      |
| Cacheable task                     | GenericCacheableTask     | @CymricTask      |

Although configuration folders will be set up automatically, element's JSON files will not be created automatically. This is to allow for more flexibility when it comes to which elements you want to use in your bot. Each element's JSON file is named `<element_id>.json`. Once the file is created, it will be populated automatically with the default values upon the next bot start.

### Specific element configuration
Other than so-called `property` configuration, elements can also have specific properties defined in their own configuration files. These properties can be set up by simply defining a field in element's class. It can sound a bit confusing, so here's an example to clear up the doubts:

```java
@CymricCommand
public class TestCommand extends GenericChatCommand {

    public TestCommand() {
        super("test");
    }

    private String someString = "This is a test command";
    private int someNumber = 67;

    @Override
    public void onInteraction(@NotNull ChatCommandContext context) {
        System.out.println(someString);
        System.out.println(someNumber);
    }
}
```
Upon populating JSON file for the first time, it will look like this:
```json
{
  "properties": {...},
  "some_string": "This is a test command",
  "some_number": 67
}
```
These values can then be edited within the JSON file, and they will be applied upon the next bot start or reload.

### Default data object configuration
As data objects tend to be quite large and spammy, Cymric offers a convenient way to configure them using `default_objects.json`. In this file, all data objects annotated with `@SupportsDefaultOverriding` will be listed with their default values.

Values edited within this file will be applied upon populating JSON file. Here is a simple example of how this works:

- This is the default ForgedChatCommandData object:
```json
{

  "options": [
    {
      "name": "",
      "description": "",
      "option_type": "STRING",
      "required": false,
      "autocomplete": false,
      "choices": [
        {
          "name": "",
          "value": ""
        }
      ],
      "min_string_length": 0,
      "max_string_length": 0,
      "min_value": 0.0,
      "max_value": 0.0
    }
  ],
  "children_ids": [],
  "command_type": "SLASH",
  "aliases": [],
  "skip_name": false,
  "registered_guild_ids": [],
  "availability_type": "GUILD",
  "integration_types": [
    "GUILD_INSTALL"
  ],
  "nsfw": false,
  "required_guild_ids": [],
  "required_user_ids": [],
  "required_channel_ids": [],
  "required_role_ids": [],
  "required_invoker_permissions": [],
  "required_bot_permissions": [],
  "talk_required": false,
  "bot_admin_required": false,
  "cooldown_millis": 0.0,
  "enabled": false,
  "error_responses": {},
  "name": "",
  "description": "",
  "defer_type": ""
}
```
- Upon populating command's JSON file, there will be a lot of unnecessary fields that user may not need. Hence, within `default_objects.json`, you can do the following:
```json
{
  "raw_data": {
    "me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData": {
      "command_type": "SLASH",
      "registered_guild_ids": [],
      "availability_type": "GUILD",
      "integration_types": [
        "GUILD_INSTALL"
      ],
      "enabled": false,
      "name": "",
      "description": "",
      "defer_type": ""
    }
  }
}
```
- Next time a command's JSON file is populated, it will only contain the fields as in the file above.
### Chat command types
Chat commands can be registered as slash, prefix or so-called hybrid commands. Hybrid commands are a combination of slash and prefix commands, meaning that they can be invoked using both slash and prefix.

Upon retrieving options within `ChatCommandContext`, option can be returned as a null. In case you are not expecting nulls, you can stop the method execution and call `onIncorrectUsage` method. This is done to allow easier integration of hybrid commands.

Chat commands can also be Response and Parent. Both have their own dedicated folders within `commands` folder:
- Response commands can have different looks but will always send a response message. They are simple commands that can be used for commands like displaying rules, information, etc.
- Parent commands serve as parent or subcommand group commands. They have no functionality on their own, but can be used to group other commands together. They are useful for organizing commands into logical groups.

### Element error responses
Upon registering a specific type of element for the first time, its configuration file will appear within `error_responses` folder. Here, you can define default responses for various error types.

If you want to add a custom error responses for a specific element, you can do so by defining the `error_responses` field within element's property configuration. It should look like:

```json
{
  "error_handlers": {
    "ERROR_TYPE": {
      // response object
    }
  }
}
```
## Placeholders
Placeholders are a powerful feature of Cymric that allows you to dynamically replace parts of your messages with specific values.

Placeholders are managed using the `PlaceholderMap`s. In case you have context at your disposal, you can retrieve the `PlaceholderMap` using `context.getPlaceholderMap()`. This map contains various placeholders that can be used in your messages, such as user IDs, channel IDs, guild IDs, etc.

Other than that, these maps can also be created using the `PlaceholderMapBuilder` class. This is useful for cases where you want to create a map without context, such as when sending a response from a task or a module.

## Responses
Responses are a big part of Cymric, as they allow you to define how your bot will respond to various events. Response itself is a collection of different actions. Each has its own data class such as `ResponseData` and `ActionData`. It is required to always use the `ResponseData` class even if it contains only one action.

Actions can do various things, such as:
- Sending a message (interaction, reply, channel send)
- Editing a message
- Deleting a message
- Sending a file
- Adding reactions
- Creating threads
- Make bot appear as typing

Actions can also be delayed, meaning that they will be executed after a certain amount of time.

Code-wise, responses can be sent using the `ResponseBuilder` class. Here's an example of response being sent within the `onInteraction` method of a chat command:

```java
@CymricCommand
public class TestCommand extends GenericChatCommand {

    public TestCommand() {
        super("test");
    }

    private ResponseData responseData = new ResponseData();

    @Override
    public void onInteraction(@NotNull ChatCommandContext context) {
        PlaceholderMap map = context.getPlaceholderMap();
        ResponseBuilder.create(map, responseData).buildAndSend();
    }
}
```

### Sending a message
This action can mean many different things, depending on the context in which it is used.
- In case of interaction contexts, this can be used as a way to respond to interaction or edit the original message (in case response has been deferred).
- In case of non-interaction context, this will simply send a message to the current channel.
- In case `channel_id` is provided, this will send a message to the specified channel.
- In case `channel_id` and `message_id` is provided and `reply` is set to true, this will reply to the specified message.

Messages can contain plain content, embeds, files and components. It is also possible to send a modal but keep in mind that modals can only be sent within interaction contexts and without response deferring.

As for components, they can be overridden within the action itself, meaning that your message can contain components with different behavior than the "parent" components. This is useful for cases where you want to have a different behavior for a specific message, such as disabling buttons, changing their labels, changing string select menu options, etc.

Here's how component overriding works:
```json
{
  // ...
  "buttons": [
    {
      "id": "test_button",
      "label": "Test Button",
      "style": "PRIMARY",
      "required_role_ids": ["{c_owner_role_id}"]
    }
  ]
}
``` 
In the example above, bot will first retrieve button with ID `test_button` and its parent configuration. It will then go through the provided `ObjectNode` and override present fields. Fields that are not present will remain as they are in the parent configuration.

### Message actions
As mentioned previously, if you want to edit or delete a message, you can either use the message provided in the current context or specify it using `channel_id` and `message_id`.

Upon editing message, you are free to use fields like `clear_content`, `clear_embeds`, `clear_files`, `clear_components` and `clear_reactions` to clear specific parts of the message.

### Message inheriting
It is possible to inherit a message from the previous action. This means that if you were to send a message to a specific channel in the previous action, you can set `inherit_message` to true in the current action and it will refer to message that was sent. Here's an example:
```json
{
  "response": [
    {
      "channel_id": "1234",
      "content": "This is the first message"
    },
    {
      "inherit_message": true,
      "delay_millis": 3000,
      "delete": true
    }
  ]
}
```
In this example, the first action will send a message to the channel with ID `1234`, and the second action will delete that message after 3 seconds.

## EventWaiter
The `EventWaiter` class facilitates awaiting events within specific classes without needing the registration of separate listener classes.

Here's an example utilizing this feature in order to await for `MessageReceivedEvent`:

```java
DiscordBot.getInstance().getEventWaiter().waitForEvent(
        MessageReceivedEvent.class, 
        event -> event.getAuthor().getName().equalsIgnoreCase("mrepiko"),
        event -> System.out.println("Hello MrEpiko, how are you today?"),
        10,
        () -> System.out.println("It seems like MrEpiko is not here :(")
);
```

## CLI commands
Bot provides a few simple CLI commands that can be used to manage the bot. These are:
- `help` - displays the list of available commands
- `reload` - reloads the bot's element configuration
- `reboot` - shuts down the bot and calls all cacheable tasks

## License
This project is licensed under Apache License 2.0. See the [LICENSE](LICENSE) file for more details.