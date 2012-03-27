package ca.lavallee.minecraft.deathmessages;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathMessages extends JavaPlugin implements Listener {
	
	private DeathMessageGenerator dmGenerator;

	@Override
	public void onEnable() {
		Logger log = getLogger();
		FileConfiguration config = getConfig();
		if (config.getConfigurationSection("DeathMessage").getKeys(false).size() == 0) {
			log.info("Writing default configuration");
			saveDefaultConfig();
			reloadConfig();
		}
		dmGenerator = new DeathMessageGenerator(getConfig(), new DeathMessageGeneratorCallback(), getLogger());
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		String player = event.getEntity().getDisplayName();
		String defaultMessage = event.getDeathMessage();
		String deathMessage = dmGenerator.generateMessage(player, defaultMessage);
		event.setDeathMessage(deathMessage);
	}
		
	class DeathMessageGeneratorCallback implements DeathMessageGenerator.Callback {

		@Override
		public String[] getOnlinePlayers() {
			Player[] players = getServer().getOnlinePlayers();
			String playerNames[] = new String[players.length];
			for (int i = 0; i < players.length; i++) {
				playerNames[i] = players[i].getDisplayName();
			}
			return playerNames;
		}
		
	}

}
