package net.PentaSteve.DiscordIntegration;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public class Bot {
    private final String serverid;
    private ServerCommandSource commandSource;
    private DiscordApi api;
    private MinecraftServer server;
    public Bot(String token,MinecraftServer server){
        this.api = new DiscordApiBuilder().setToken(token).login().join();
        api.addMessageCreateListener(event->{ handleMessage(event); });
        this.serverid = "[" + DiscordIntegration.serverid + "]";
        this.server = server;
        this.commandSource = server.getCommandSource();
    }

    public void handleMessage(MessageCreateEvent event){
        if (event.getMessageContent().equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!");
        }
        if (event.getChannel().getId() == DiscordIntegration.chatBridge.getId()){
            //System.out.println("A message was received in the chat bridge channel");
            if(DiscordIntegration.multiservermode){
                if(!event.getMessageContent().split(" ")[0].equals(this.serverid)){
                    //System.out.println("attempting to send message in chat");
                    this.server.getCommandManager().execute(this.commandSource,this.formatAsTellraw(event.getMessageContent(),event.getMessageAuthor().getDisplayName()));
                }
            } else {
                this.server.getCommandManager().execute(this.commandSource,this.formatAsTellraw(event.getMessageContent(),event.getMessageAuthor().getDisplayName()));
            }
        }
    }
    public void onServerInit(ServerLifecycleEvents.ServerStarted event){

    }

    public void sendMessage(String message, TextChannel channel){
        channel.sendMessage(message);
    }
    public DiscordApi getApi(){
        return api;
    }
    private String formatAsTellraw(String message, String sender){
        return "/tellraw @a {" + "\"text\":\"[Discord] <"+sender+"> "+message+"\"}";
    }
}
