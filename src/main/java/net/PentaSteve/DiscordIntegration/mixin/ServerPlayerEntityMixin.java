package net.PentaSteve.DiscordIntegration.mixin;

import com.mojang.authlib.GameProfile;
import net.PentaSteve.DiscordIntegration.chatMessageHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntity{
    private World world;
    private ServerPlayNetworkHandler networkHandler;
    private MinecraftServer server;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "onDeath(L;)V",at=@At("HEAD"), remap = false, cancellable = true)
    public void onDeath(DamageSource source, CallbackInfo ci) {
        boolean bl = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        if (bl) {
            Text text = this.getDamageTracker().getDeathMessage();
            chatMessageHandler.sendchatmessage(text.getString()); // added for discord integration
            this.networkHandler.sendPacket(new CombatEventS2CPacket(this.getDamageTracker(), CombatEventS2CPacket.Type.ENTITY_DIED, text), (future) -> {
                if (!future.isSuccess()) {
                    String string = text.asTruncatedString(256);
                    Text text2 = new TranslatableText("death.attack.message_too_long", new Object[]{(new LiteralText(string)).formatted(Formatting.YELLOW)});
                    Text text3 = (new TranslatableText("death.attack.even_more_magic", new Object[]{this.getDisplayName()})).styled((style) -> {
                        return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2));
                    });
                    this.networkHandler.sendPacket(new CombatEventS2CPacket(this.getDamageTracker(), CombatEventS2CPacket.Type.ENTITY_DIED, text3));
                }

            });
            AbstractTeam abstractTeam = this.getScoreboardTeam();
            if (abstractTeam != null && abstractTeam.getDeathMessageVisibilityRule() != AbstractTeam.VisibilityRule.ALWAYS) {
                if (abstractTeam.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerManager().sendToTeam(this, text);
                } else if (abstractTeam.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerManager().sendToOtherTeams(this, text);
                }
            } else {
                this.server.getPlayerManager().broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
            }
        } else {
            this.networkHandler.sendPacket(new CombatEventS2CPacket(this.getDamageTracker(), CombatEventS2CPacket.Type.ENTITY_DIED));
        }

        this.dropShoulderEntities();
        if (this.world.getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.forgiveMobAnger();
        }

        if (!this.isSpectator()) {
            this.drop(source);
        }

        this.getScoreboard().forEachScore(ScoreboardCriterion.DEATH_COUNT, this.getEntityName(), ScoreboardPlayerScore::incrementScore);
        LivingEntity livingEntity = this.getPrimeAdversary();
        if (livingEntity != null) {
            this.incrementStat(Stats.KILLED_BY.getOrCreateStat(livingEntity.getType()));
            livingEntity.updateKilledAdvancementCriterion(this, this.scoreAmount, source);
            this.onKilledBy(livingEntity);
        }

        this.world.sendEntityStatus(this, (byte)3);
        this.incrementStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        this.extinguish();
        this.setFlag(0, false);
        this.getDamageTracker().update();
        ci.cancel();
    }

    private void forgiveMobAnger() {
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }


}
