package net.PentaSteve.DiscordIntegration.mixin;

import net.PentaSteve.DiscordIntegration.chatMessageHandler;
import net.minecraft.SharedConstants;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow private ServerPlayerEntity player;
    @Shadow private MinecraftServer server;
    @Shadow private int messageCooldown;

    public ServerPlayNetworkHandlerMixin(ServerPlayerEntity player, MinecraftServer server){
        this.player = player;
        this.server = server;
    }
    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at=@At("HEAD"),remap = false,cancellable = true)
    public void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        //NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID));
        } else {
            this.player.updateLastActionTime();
            String string = StringUtils.normalizeSpace(packet.getChatMessage());

            for(int i = 0; i < string.length(); ++i) {
                if (!SharedConstants.isValidChar(string.charAt(i))) {
                    this.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                    return;
                }
            }

            if (string.startsWith("/")) {
                this.executeCommand(string);
            } else {
                Text text = new TranslatableText("chat.type.text", new Object[]{this.player.getDisplayName(), string});
                this.server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, this.player.getUuid());
                chatMessageHandler.sendchatmessage("<"+player.getName().getString()+"> " + string); // added for discord integration
            }

            this.messageCooldown += 20;
            if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile())) {
                this.disconnect(new TranslatableText("disconnect.spam"));
            }

        }
        ci.cancel();
    }

    private void executeCommand(String string) { }

    public void disconnect(TranslatableText translatableText) { }
    @Shadow public void sendPacket(Packet<?> packet){ }
}
