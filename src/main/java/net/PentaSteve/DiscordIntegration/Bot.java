package net.PentaSteve.DiscordIntegration;


import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public class Bot {
    private DiscordApi api;
    public Bot(String token){
        this.api = new DiscordApiBuilder().setToken(token).login().join();
        api.addMessageCreateListener(event->{ handleMessage(event); });
    }

    private void handleMessage(MessageCreateEvent event){
        if (event.getMessageContent().equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!");
        }
    }

    public void sendMessage(String message, TextChannel channel){
        channel.sendMessage(message);
    }
    public DiscordApi getApi(){
        return api;
    }

}
