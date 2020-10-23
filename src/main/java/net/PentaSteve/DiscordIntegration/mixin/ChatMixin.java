package net.PentaSteve.DiscordIntegration.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ChatMixin {
    private final MinecraftServer server;
    @Shadow
    public ServerPlayerEntity player;

    @Shadow protected abstract void executeCommand(String input);

    public ChatMixin(MinecraftServer server){
        this.server = server;
    }

    /**
     * Passes our chat message to the chat bridge
     */
    @Inject(method = "onGameMessage", at = @At(value = "HEAD"), cancellable = true)
    public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci){
        String chatMessage = packet.getChatMessage();
        String playerName = this.player.getDisplayName().asString();

        if (chatMessage.startsWith("/")) {
            this.executeCommand(chatMessage);
        } else {
            Text text = new TranslatableText("chat.type.text", new Object[]{this.player.getDisplayName(), chatMessage});
            this.server.getPlayerManager().broadcastChatMessage(text, MessageType.SYSTEM, this.player.getUuid());
        }
        System.out.println(playerName + " " + chatMessage);
        ci.cancel();
    }

}
