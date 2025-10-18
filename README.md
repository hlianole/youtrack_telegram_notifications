# YouTrack Notifications Telegram bot
___
## Usage:

You will need to configure the project first.

In `/srs/main/resources` create an `application.conf` file and copy the content 
of `/srs/main/resources/application.conf.example` there. 

### Values:

`youtrack.url` is the base url of your YouTrack instance. Must include `https://`. 
Example: `"https://example.youtrack.cloud"`

`youtrack.token` is your YouTrack permanent token. 
Generate at **Profile** -> **Account Security**, **Tokens**, **New token...**

`telegram.botToken` is your Telegram bot token. 
First, create a bot: in Telegram find `@BotFather`. Follow the instructions in the bot creation.
After choosing the bot's username you will get a message containing your token. 
You will further use this new created bot.

`telegram.allowedChatIds` is the coma separated list of ids of the Telegram chats you allow the 
bot to get messages from. You can find your chat id by using `@userinfobot`. Example: 
"123456789,-1234567890000,987654321".

### How to run:

After successful configuring the application you can start using it. 

Build the project from the main directory:

```bash
./gradlew clean build
```

Then run it using:

```bash
./gradlew run
```

Or you can also run it from the IDE.

### Starting:

In Telegram chat (one of the allowed chats from configuration) add your created bot 
and use the command `/start`. This command will show the use cases of the bot.
You can also simply start the private chat with your bot.
If the bot is used from any chat except allowed, the user will get the message 
"You are not allowed to use this bot".

### Commands:

`/start` show the main info about how to use the bot

`/start-poll` will start checking for the new notifications from your YouTrack 
instance every 60 seconds. If There are, the bot will send them to the chat.

`/interval <seconds>` will change the by-default checking interval from 60 seconds to
your value

`/stop-poll` will stop checkin for the new notifications

`/notifications` is used to check for the new notifications immediately,
without waiting. Can be used without starting the polling

`/create <project-id-(short-name)> <summary>` will create a new issue for the project
<project-id-(short-name)> with the summary provided in <summary>
