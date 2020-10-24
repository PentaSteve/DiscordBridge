package net.PentaSteve.DiscordIntegration;

import com.oroarmor.util.config.Config;
import com.oroarmor.util.config.ConfigItem;
import com.oroarmor.util.config.ConfigItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DiscordIntegration implements ModInitializer {

	private String token;
	public static String serverid;
	public static boolean multiservermode;
	private String channelName;
	private String guildName;
	private Server server;
	public static ServerTextChannel chatBridge;
	public static Bot bot;

	@Override
	public void onInitialize() {
		//  Initialize configs
		File configFile = new File(String.valueOf(FabricLoader.getInstance().getConfigDir()) + "/DiscordIntegration.json");
		List<ConfigItem<?>> configs = new ArrayList<>();
		configs.add(new ConfigItem<>("Discord_bot_token", "Put discord bot token here", "bot token"));
		configs.add(new ConfigItem<>("Server_ID", "Survival", "This is only used for multi-server mode"));
		configs.add(new ConfigItem<>("Multi-server_mode", false, "enable multi-server mode. this requires all servers to have a unique server id."));
		configs.add(new ConfigItem<>("Discord channel name", "Channel name", "the name of the channel to put chat messages in"));
		configs.add(new ConfigItem<>("Guild Name", "guild id", "the id of the guild (server) to put chat messages in."));
		List<ConfigItemGroup> configGroups = new ArrayList<>();
		configGroups.add(new ConfigItemGroup(configs, "Discord Integration Options"));

		Config config = new Config(configGroups, configFile);
		if (!configFile.exists()) {
			System.out.println("[Discord Integration] creating config file");
			config.saveConfigToFile();
		}
		config.readConfigFromFile();

		configGroups = config.getConfigs();

		configs = configGroups.get(0).getConfigs();

		try {
			this.token = configs.get(0).getValue().toString();
			serverid = configs.get(1).getValue().toString();
			//System.out.println(configs.get(2).getValue().toString());

			if (configs.get(2).getValue().toString() == "false") {
				multiservermode = false;
			} else if (configs.get(2).getValue().toString() == "true") {
				multiservermode = true;
			}

			this.channelName = configs.get(3).getValue().toString();
			this.guildName = configs.get(4).getValue().toString();
		} catch (Exception e) {
			System.out.println("[Discord Integration] encountered an error while reading the config file");
		}
		/*
		System.out.println(this.token);
		System.out.println(this.serverid);
		System.out.println(this.multiservermode);
		*/
		// initialize discord bot

		bot = new Bot(this.token);
		for(Iterator i = bot.getApi().getServerTextChannelsByNameIgnoreCase(this.channelName).iterator(); i.hasNext();) {
			chatBridge = (ServerTextChannel) i.next();
		}
	}
	
}
