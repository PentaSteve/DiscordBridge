package net.PentaSteve.DiscordIntegration;

import com.oroarmor.util.config.Config;
import com.oroarmor.util.config.ConfigItem;
import com.oroarmor.util.config.ConfigItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import net.dv8ion.jda.api.JDA;
//import net.dv8ion.jda.api.JDABuilder;
//import javax.security.auth.login.LoginException;
//import JDA;

public class DiscordIntegration implements ModInitializer {

	private String token;
	private String serverid;
	private boolean multiservermode;
	private JDABuilder builder;

	@Override
	public void onInitialize() {
		//  Initialize configs
		File configFile = new File(String.valueOf(FabricLoader.getInstance().getConfigDir())+"/DiscordIntegration.json");
		List<ConfigItem<?>> configs = new ArrayList<>();
		configs.add(new ConfigItem<>("Discord_bot_token","Put discord bot token here","bot token"));
		configs.add(new ConfigItem<>("Server_ID", "Survival","This is only used for multi-server mode"));
		configs.add(new ConfigItem<>("Multi-server_mode", false, "enable multi-server mode. this requires all servers to have a unique server id."));
		List<ConfigItemGroup> configGroups = new ArrayList<>();
		configGroups.add(new ConfigItemGroup(configs, "Discord Integration Options"));

		Config config = new Config(configGroups, configFile);
		if(!configFile.exists()) {
			System.out.println("[Discord Integration] creating config file");
			config.saveConfigToFile();
		}
		config.readConfigFromFile();

		configGroups = config.getConfigs();

		configs = configGroups.get(0).getConfigs();

		try {
			this.token = (String) configs.get(0).getValue();
			this.serverid = (String) configs.get(1).getValue();
			//System.out.println(configs.get(2).getValue().toString());
			if(configs.get(2).getValue().toString() == "false"){ this.multiservermode = false; }
			else if(configs.get(2).getValue().toString() == "true"){ this.multiservermode = true; }

		}
		catch (Exception e){
			System.out.println("[Discord Integration] encountered an error while reading the config file");
		}
		System.out.println(this.token);
		System.out.println(this.serverid);
		System.out.println(this.multiservermode);
		// initialize discord bot
		this.builder = new JDABuilder(AccountType.BOT);
		this.builder.setToken(this.token);
		try {
			this.builder.build();
		}
		catch (javax.security.auth.login.LoginException ignored){ }

	}
}
