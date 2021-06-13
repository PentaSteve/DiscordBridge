package net.PentaSteve.DiscordBridge;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class Bot {
    private final String serverid;
    private ServerCommandSource commandSource;
    private JDA jda;
    private MinecraftServer server;
    private TextChannel chatBridge;

    public Bot(String token,MinecraftServer server,String channelId){
        try{
            this.jda = JDABuilder.createDefault(token).build();
        } catch (LoginException e){
            e.printStackTrace();
        }
        this.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                Bot.this.handleMessage(event);
            }
        });
        this.serverid = "[" + ChatBridge.serverid + "]";
        this.server = server;
        this.commandSource = server.getCommandSource();
        new Thread(()->{
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(channelId != null){
                this.chatBridge = jda.getTextChannelById(channelId);
                System.out.println(this.chatBridge.getName());
            }
        }).start();

    }

    public void handleMessage(MessageReceivedEvent event){
        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }
        if (event.getChannel().getId().equals(this.chatBridge.getId())){
            if(!messageContent.startsWith(this.serverid)){
                if(event.getAuthor() == this.jda.getSelfUser()){
                    this.server.getCommandManager().execute(this.commandSource,formatAsTellraw(messageContent));
                } else {
                    this.server.getCommandManager().execute(this.commandSource,formatAsTellraw(String.format("<%s> %s",(event.getMember().getNickname() != null ? event.getMember().getNickname() : event.getAuthor().getName()),messageContent)));
                }
            }
        }
    }
    public void onServerInit(ServerLifecycleEvents.ServerStarted event){

    }

    public void sendMessage(String message){
        this.chatBridge.sendMessage(this.serverid + " " + message).queue();
    }

    private String formatAsTellraw(String message){
        return String.format("/tellraw @a {\"text\":\"[DISCORD] %s\"}",message);//"/tellraw @a {" + "\"text\":\"[DISCORD] <"+sender+"> "+message+"\"}";
    }
}
