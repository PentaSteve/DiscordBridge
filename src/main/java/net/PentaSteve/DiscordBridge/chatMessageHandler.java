package net.PentaSteve.DiscordBridge;

public abstract class chatMessageHandler {

    public static void sendchatmessage(String message) {
        ChatBridge.bot.sendMessage(message);
    }
}
