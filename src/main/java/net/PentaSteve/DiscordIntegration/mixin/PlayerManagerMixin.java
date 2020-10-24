package net.PentaSteve.DiscordIntegration.mixin;


import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.PentaSteve.DiscordIntegration.chatMessageHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.SERVER)
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final protected int maxPlayers;
    @Shadow @Final private DynamicRegistryManager.Impl registryManager;
    @Shadow private final MinecraftServer server;
    @Shadow private final List<ServerPlayerEntity> players;
    @Shadow @Final private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();
    @Shadow private int viewDistance;
    @Shadow private final Map<UUID, ServerStatHandler> statisticsMap;
    @Shadow private final Map<UUID, PlayerAdvancementTracker> advancementTrackers;

    public PlayerManagerMixin(MinecraftServer server, List<ServerPlayerEntity> players, Map<UUID, ServerStatHandler> statisticsMap, Map<UUID, PlayerAdvancementTracker> advancementTrackers){
        this.server = server;
        this.players = players;
        this.statisticsMap = statisticsMap;
        this.advancementTrackers = advancementTrackers;
    }

    @Shadow @Final private static Logger LOGGER;


    @SuppressWarnings("all")
    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", at=@At("HEAD"),remap = false,cancellable = true)
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        GameProfile gameProfile = player.getGameProfile();
        UserCache userCache = this.server.getUserCache();
        GameProfile gameProfile2 = userCache.getByUuid(gameProfile.getId());
        String string = gameProfile2 == null ? gameProfile.getName() : gameProfile2.getName();
        userCache.add(gameProfile);
        CompoundTag compoundTag = this.loadPlayerData(player);
        RegistryKey var23;
        if (compoundTag != null) {
            DataResult var10000 = DimensionType.method_28521(new Dynamic(NbtOps.INSTANCE, compoundTag.get("Dimension")));
            Logger var10001 = LOGGER;
            var10001.getClass();
            var23 = (RegistryKey)var10000.resultOrPartial(var10001::error).orElse(World.OVERWORLD);
        } else {
            var23 = World.OVERWORLD;
        }

        RegistryKey<World> registryKey = var23;
        ServerWorld serverWorld = this.server.getWorld(registryKey);
        ServerWorld serverWorld3;
        if (serverWorld == null) {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", registryKey);
            serverWorld3 = this.server.getOverworld();
        } else {
            serverWorld3 = serverWorld;
        }

        player.setWorld(serverWorld3);
        player.interactionManager.setWorld((ServerWorld)player.world);
        String string2 = "local";
        if (connection.getAddress() != null) {
            string2 = connection.getAddress().toString();
        }

        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), string2, player.getEntityId(), player.getX(), player.getY(), player.getZ());
        WorldProperties worldProperties = serverWorld3.getLevelProperties();
        this.setGameMode(player, (ServerPlayerEntity)null, serverWorld3);
        ServerPlayNetworkHandler serverPlayNetworkHandler = new ServerPlayNetworkHandler(this.server, connection, player);
        GameRules gameRules = serverWorld3.getGameRules();
        boolean bl = gameRules.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
        boolean bl2 = gameRules.getBoolean(GameRules.REDUCED_DEBUG_INFO);
        serverPlayNetworkHandler.sendPacket(new GameJoinS2CPacket(player.getEntityId(), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), BiomeAccess.hashSeed(serverWorld3.getSeed()), worldProperties.isHardcore(), this.server.getWorldRegistryKeys(), this.registryManager, serverWorld3.getDimension(), serverWorld3.getRegistryKey(), this.getMaxPlayerCount(), this.viewDistance, bl2, !bl, serverWorld3.isDebugWorld(), serverWorld3.isFlat()));
        serverPlayNetworkHandler.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(this.getServer().getServerModName())));
        serverPlayNetworkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        serverPlayNetworkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.abilities));
        serverPlayNetworkHandler.sendPacket(new HeldItemChangeS2CPacket(player.inventory.selectedSlot));
        serverPlayNetworkHandler.sendPacket(new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().values()));
        serverPlayNetworkHandler.sendPacket(new SynchronizeTagsS2CPacket(this.server.getTagManager()));
        this.sendCommandTree(player);
        player.getStatHandler().updateStatSet();
        player.getRecipeBook().sendInitRecipesPacket(player);
        this.sendScoreboard(serverWorld3.getScoreboard(), player);
        this.server.forcePlayerSampleUpdate();
        TranslatableText mutableText2;
        if (player.getGameProfile().getName().equalsIgnoreCase(string)) {
            mutableText2 = new TranslatableText("multiplayer.player.joined", new Object[]{player.getDisplayName()});
        } else {
            mutableText2 = new TranslatableText("multiplayer.player.joined.renamed", new Object[]{player.getDisplayName(), string});
        }

        this.broadcastChatMessage(mutableText2.formatted(Formatting.YELLOW), MessageType.SYSTEM, Util.NIL_UUID);
        chatMessageHandler.sendchatmessage(player.getName().getString()+" joined the game"); // added for discord integration
        serverPlayNetworkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
        this.players.add(player);
        this.playerMap.put(player.getUuid(), player);
        this.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, new ServerPlayerEntity[]{player}));

        for(int i = 0; i < this.players.size(); ++i) {
            player.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, new ServerPlayerEntity[]{(ServerPlayerEntity)this.players.get(i)}));
        }

        serverWorld3.onPlayerConnected(player);
        this.server.getBossBarManager().onPlayerConnect(player);
        this.sendWorldInfo(player, serverWorld3);
        if (!this.server.getResourcePackUrl().isEmpty()) {
            player.sendResourcePackUrl(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
        }

        Iterator var24 = player.getStatusEffects().iterator();

        while(var24.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var24.next();
            serverPlayNetworkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), statusEffectInstance));
        }

        if (compoundTag != null && compoundTag.contains("RootVehicle", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("RootVehicle");
            Entity entity = EntityType.loadEntityWithPassengers(compoundTag2.getCompound("Entity"), serverWorld3, (vehicle) -> {
                return !serverWorld3.tryLoadEntity(vehicle) ? null : vehicle;
            });
            if (entity != null) {
                UUID uUID2;
                if (compoundTag2.containsUuid("Attach")) {
                    uUID2 = compoundTag2.getUuid("Attach");
                } else {
                    uUID2 = null;
                }

                Iterator var21;
                Entity entity3;
                if (entity.getUuid().equals(uUID2)) {
                    player.startRiding(entity, true);
                } else {
                    var21 = entity.getPassengersDeep().iterator();

                    while(var21.hasNext()) {
                        entity3 = (Entity)var21.next();
                        if (entity3.getUuid().equals(uUID2)) {
                            player.startRiding(entity3, true);
                            break;
                        }
                    }
                }

                if (!player.hasVehicle()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    serverWorld3.removeEntity(entity);
                    var21 = entity.getPassengersDeep().iterator();

                    while(var21.hasNext()) {
                        entity3 = (Entity)var21.next();
                        serverWorld3.removeEntity(entity3);
                    }
                }
            }
        }

        player.onSpawn();
        ci.cancel();
    }


    @Inject(method = "remove(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at=@At("HEAD"), cancellable = true, remap = false)
    public void remove(ServerPlayerEntity player, CallbackInfo ci) {
        ServerWorld serverWorld = player.getServerWorld();
        player.incrementStat(Stats.LEAVE_GAME);
        this.savePlayerData(player);
        if (player.hasVehicle()) {
            Entity entity = player.getRootVehicle();
            if (entity.hasPlayerRider()) {
                LOGGER.debug("Removing player mount");
                player.stopRiding();
                serverWorld.removeEntity(entity);
                entity.removed = true;

                Entity entity2;
                for(Iterator var4 = entity.getPassengersDeep().iterator(); var4.hasNext(); entity2.removed = true) {
                    entity2 = (Entity)var4.next();
                    serverWorld.removeEntity(entity2);
                }

                serverWorld.getChunk(player.chunkX, player.chunkZ).markDirty();
            }
        }

        player.detach();
        serverWorld.removePlayer(player);
        player.getAdvancementTracker().clearCriteria();
        this.players.remove(player);
        this.server.getBossBarManager().onPlayerDisconnect(player);
        UUID uUID = player.getUuid();
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.playerMap.get(uUID);
        if (serverPlayerEntity == player) {
            this.playerMap.remove(uUID);
            this.statisticsMap.remove(uUID);
            this.advancementTrackers.remove(uUID);
        }

        this.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, new ServerPlayerEntity[]{player}));
        chatMessageHandler.sendchatmessage(player.getName().getString() + " left the game"); // added for discord integration
        ci.cancel();
    }

    @Shadow protected void savePlayerData(ServerPlayerEntity player) { };
    @Shadow public int getMaxPlayerCount() { return maxPlayers; };
    @Shadow public MinecraftServer getServer() { return this.server; };
    @Shadow public void sendWorldInfo(ServerPlayerEntity player, ServerWorld serverWorld3) {};
    public void sendToAll(PlayerListS2CPacket playerListS2CPacket) {};
    public void broadcastChatMessage(MutableText formatted, MessageType system, UUID nilUuid) {};
    @Shadow protected void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player) {};
    @Shadow public void sendCommandTree(ServerPlayerEntity player){};
    @Shadow private void setGameMode(ServerPlayerEntity player, ServerPlayerEntity serverPlayerEntity, ServerWorld serverWorld3){ }
    @Shadow public CompoundTag loadPlayerData(ServerPlayerEntity player){ return new CompoundTag(); }
    /*

    @Inject(method = "sendToAll", at = @At(value = "HEAD"), cancellable = true)
    public void sendToAll(Packet<?> packet, CallbackInfo ci){
        for(int i = 0; i < this.players.size(); ++i) {
            ((ServerPlayerEntity)this.players.get(i)).networkHandler.sendPacket(packet);
        }
        chatMessageHandler.sendchatmessage(packet);
        ci.cancel();
    }
    */
}
