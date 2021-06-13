package net.PentaSteve.DiscordBridge;

import com.oroarmor.util.config.Config;
import com.oroarmor.util.config.ConfigItem;
import com.oroarmor.util.config.ConfigItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ChatBridge implements ModInitializer {

	private String token;
	public static String serverid;
	private String channelID;
	//MinecraftServer Server;
	public static Bot bot;

	@Override
	public void onInitialize() {
		//  Initialize configs
		File configFile = new File(String.valueOf(FabricLoader.getInstance().getConfigDir()) + "/DiscordIntegration.json");
		List<ConfigItem<?>> configs = new ArrayList<>();
		configs.add(new ConfigItem<>("Discord_bot_token", "Put discord bot token here", "bot token"));
		configs.add(new ConfigItem<>("Server_ID", "SMP", "The minecraft server's unique name"));
		configs.add(new ConfigItem<>("Discord channel ID", "Put chat bridge channel id here", "chat bridge channel id"));
		List<ConfigItemGroup> configGroups = new ArrayList<>();
		configGroups.add(new ConfigItemGroup(configs, "Chat Bridge Options"));

		Config config = new Config(configGroups, configFile);
		if (!configFile.exists()) {
			System.out.println("[Discord Bridge] creating config file");
			config.saveConfigToFile();
		}
		config.readConfigFromFile();

		configGroups = config.getConfigs();

		configs = configGroups.get(0).getConfigs();

		try {
			this.token = configs.get(0).getValue().toString();
			serverid = configs.get(1).getValue().toString();
			//System.out.println(configs.get(2).getValue().toString());
			this.channelID = (String) configs.get(2).getValue();
		} catch (Exception e) {
			e.printStackTrace();
			//System.exit(0);
			System.out.println("[Discord Bridge] encountered an error while reading the config file");
		}
		/*
		System.out.println(this.token);
		System.out.println(this.serverid);
		System.out.println(this.multiservermode);
		*/
		// initialize discord bot after server initialization
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			bot = new Bot(this.token,server,this.channelID);
		});
	}
}
