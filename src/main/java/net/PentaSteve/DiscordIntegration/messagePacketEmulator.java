package net.PentaSteve.DiscordIntegration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.network.listener.ClientPlayPacketListener;

import java.io.IOException;
import java.util.UUID;

public class messagePacketEmulator extends GameMessageS2CPacket {
    private Text message;
    private MessageType location;
    private UUID senderUuid;

    public messagePacketEmulator() {
    }

    public messagePacketEmulator(GameMessageS2CPacket packet) {

    }

    public void read(PacketByteBuf buf) throws IOException {
        this.message = buf.readText();
        this.location = MessageType.byId(buf.readByte());
        this.senderUuid = buf.readUuid();
    }

    public void write(PacketByteBuf buf) throws IOException {
        buf.writeText(this.message);
        buf.writeByte(this.location.getId());
        buf.writeUuid(this.senderUuid);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {

    }

    public Text getMessage() {
        return this.message;
    }

    public boolean isNonChat() {
        return this.location == MessageType.SYSTEM || this.location == MessageType.GAME_INFO;
    }

    public MessageType getLocation() {
        return this.location;
    }

    public UUID getSender() {
        return this.senderUuid;
    }

    public boolean isWritingErrorSkippable() {
        return true;
    }
}