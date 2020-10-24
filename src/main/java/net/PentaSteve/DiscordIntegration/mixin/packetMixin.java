package net.PentaSteve.DiscordIntegration.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Environment(EnvType.SERVER)
@Mixin(GameMessageS2CPacket.class)
public abstract class packetMixin {
    /*
    @Shadow private UUID senderUuid;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getSender()", at = @At("HEAD"))
    public UUID getSender(CallbackInfo ci){
        return this.senderUuid;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getMessageText()", at = @At("HEAD"))
    public UUID getMessageText(CallbackInfo ci){
        return this.senderUuid;
    }*/
}
