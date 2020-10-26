package net.PentaSteve.DiscordIntegration;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.MutableText;

import java.io.IOException;

public abstract class chatMessageHandler {

    public static void sendchatmessage(String message) {
        //system.out.println(message);
        if (DiscordIntegration.multiservermode) {
            DiscordIntegration.bot.sendMessage("[" + DiscordIntegration.serverid + "] " + message, DiscordIntegration.chatBridge);
        } else {
            DiscordIntegration.bot.sendMessage(message, DiscordIntegration.chatBridge);
        }
    }
}
