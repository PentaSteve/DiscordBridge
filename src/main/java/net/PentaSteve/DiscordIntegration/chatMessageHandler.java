package net.PentaSteve.DiscordIntegration;

import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.MutableText;

public class chatMessageHandler {

    public static void sendchatmessage(String message) {
        System.out.println(message);
        if(DiscordIntegration.multiservermode){
            DiscordIntegration.bot.sendMessage("["+DiscordIntegration.serverid+"] "+message, DiscordIntegration.chatBridge);
        } else {
            DiscordIntegration.bot.sendMessage(message, DiscordIntegration.chatBridge);
        }
    }

    private static void handlemessage(GameMessageS2CPacket pack){
        /*
        String fMessage = "error: no message type given";
        try {
            switch (pack.getLocation()) {
                case CHAT: {
                    if (DiscordIntegration.multiservermode) { fMessage = "[" + DiscordIntegration.serverid + "] <" + pack.getSender() + "> " + pack.getMessageText(); }
                    else { fMessage = "<" + pack.getSender() + "> " + pack.getMessageText(); }
                }
                case SYSTEM: {
                    fMessage = "System message";
                }
                case GAME_INFO: {
                    fMessage = "Game info message";
                }
            }
            DiscordIntegration.bot.sendMessage(fMessage, DiscordIntegration.chatBridge);
        } catch (NoSuchMethodError e){
            e.printStackTrace();
        }
        */
    }
}
