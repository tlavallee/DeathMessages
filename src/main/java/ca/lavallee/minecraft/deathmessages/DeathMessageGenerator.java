package ca.lavallee.minecraft.deathmessages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public class DeathMessageGenerator {
	public static interface Callback {
		public String[] getOnlinePlayers();
	}
	private final Logger log;
	private Map<String, String> defaultMessages = new HashMap<String, String>();
	private Map<String, List<String>> alternateMessages = new HashMap<String, List<String>>();
	private Random rnd;
	private Map<String, String> lastMessage = new HashMap<String, String>();
	private final Callback callback;
	

	public DeathMessageGenerator(FileConfiguration config, Callback callback, Logger log) {
		this.log = log;
		this.callback = callback;
		rnd = new Random(System.currentTimeMillis());
		Set<String> deathTypes = config.getConfigurationSection("DeathMessage").getKeys(false);
		for (String deathType: deathTypes) {
			String defaultMessage = config.getString("DeathMessage." + deathType + ".Default");
			defaultMessages.put(deathType,  config.getString("DeathMessage." + deathType + ".Default"));
			@SuppressWarnings("unchecked")
			List<String> alternates = (List<String>) config.getList("DeathMessage." + deathType + ".Alternates");
			if (config.getBoolean("DeathMessage." + deathType + ".UseDefault", true)) {
				alternates.add("%p " + defaultMessage);
			}
			alternateMessages.put(deathType, alternates);
			log.info("Death Type: " + deathType + " has " + alternates.size() + " possible messages");
		}
	}
	
	public String generateMessage(String playerName, String message) {
		String deathType = null;
		String deathMessage = message;
		String killer = null;
		String[] players = null;
		for (String dt: defaultMessages.keySet()) {
			String dm = defaultMessages.get(dt);
			if (dm.contains("%k")) {
				continue;
			}
			if (message.contains(dm)) {
				deathType = dt;
				break;
			}
		}
		if (deathType == null) {
			for (String dt: defaultMessages.keySet()) {
				String dm = defaultMessages.get(dt);
				if (!dm.contains("%k")) {
					continue;
				}
				dm = dm.substring(0, dm.indexOf("%k"));
				if (!message.contains(dm)) {
					continue;
				}
				deathType = dt;
				killer = message.substring(message.indexOf(dm) + dm.length());
				if (players == null) {
					players = callback.getOnlinePlayers();
					boolean killermob = true;
					for (String player: players) {
						if (player.equals(killer)) {
							killermob = false;
							break;
						}
					}
					if (killermob) {
						dt = null;
					}
				}
				break;
			}
		}
		if (deathType == null) {
			log.info("Unknown death type: '" + message + "'");
		}
		else if (deathType != null) {
			List<String> alternates = alternateMessages.get(deathType);
			boolean picked = false;
			do {
				int i = rnd.nextInt(alternates.size());
				deathMessage = alternates.get(i);
				if (deathMessage.contains("%r")) {
					if (players == null) {
						players = callback.getOnlinePlayers();
					}
					if (players.length < 2) {
						continue;
					}
				}
				if (alternates.size() > 1) {
					/*
					 * Don't display the same death message twice in a row.
					 */
					if (deathMessage.equals(lastMessage.get(deathType))) {
						continue;
					}
					lastMessage.put(deathType, deathMessage);
				}
				picked = true;
			} while (!picked);
			deathMessage = deathMessage.replaceAll("%p", playerName);
			if (deathMessage.contains("%k")) {
				deathMessage = deathMessage.replaceAll("%k", killer);
			}
			
			if (deathMessage.contains("%r")) {
				String randomPlayer = null;
				picked = false;
				do {
					int i = rnd.nextInt(players.length);
					randomPlayer = players[i];
					if (randomPlayer.equals(playerName)) {
						continue;
					}
					picked = true;
				} while (!picked);
				deathMessage = deathMessage.replaceAll("%r", randomPlayer);
			}
		}
		return deathMessage;
	}
	
}
