package net.PentaSteve.DiscordIntegration.mixin;


import com.google.common.collect.Maps;
import net.PentaSteve.DiscordIntegration.chatMessageHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.SERVER)
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow private final MinecraftServer server;
    @Shadow private final List<ServerPlayerEntity> players;
    @Shadow private final Map<UUID, ServerStatHandler> statisticsMap;
    @Shadow private final Map<UUID, PlayerAdvancementTracker> advancementTrackers;

    public PlayerManagerMixin(MinecraftServer server, List<ServerPlayerEntity> players, Map<UUID, ServerStatHandler> statisticsMap, Map<UUID, PlayerAdvancementTracker> advancementTrackers){
        this.server = server;
        this.players = players;
        this.statisticsMap = statisticsMap;
        this.advancementTrackers = advancementTrackers;
    }

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    public void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        this.server.sendSystemMessage(message, senderUuid);
        chatMessageHandler.sendchatmessage(message.getString()); // added for discord integration
        this.sendToAll(new GameMessageS2CPacket(message, type, senderUuid));
        ci.cancel();
    }

    @Shadow public void sendToAll(Packet<?> packet) {};
}
