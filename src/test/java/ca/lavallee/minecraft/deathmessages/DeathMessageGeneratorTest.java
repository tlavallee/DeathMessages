package ca.lavallee.minecraft.deathmessages;

import static org.junit.Assert.assertFalse;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Test;

public class DeathMessageGeneratorTest {
	private Logger log = Logger.getAnonymousLogger();
	private FileConfiguration config;

	@Before
	public void setUp() throws Exception {
		System.out.println("user.home: " + System.getProperty("user.home"));
		System.out.println("user.dir: " + System.getProperty("user.dir"));
		YamlConfiguration yconfig = new YamlConfiguration();
		yconfig.load(getClass().getResourceAsStream("/config.yml"));
		config = yconfig;
	}

	@Test
	public void test() {
		DeathMessageGenerator.Callback dmCallback = new DeathMessageGenerator.Callback() {			
			@Override
			public String[] getOnlinePlayers() {
				return new String[] {"Beard_Rinker",
					"realwaba",
					"ravenpaw256",
					"mathew15k"};
			}
		};
		DeathMessageGenerator dmg = new DeathMessageGenerator(config, dmCallback, log);
		Set<String> deathTypes = config.getConfigurationSection("DeathMessage").getKeys(false);
		
		for (String deathKey: deathTypes) {
			log.info("Death Type: " + deathKey);
			String defaultMessage = config.getString("DeathMessage." + deathKey + ".Default");
			if (defaultMessage.contains("%k")) {
				defaultMessage = defaultMessage.replaceAll("%k", "realwaba");
			}
			String lastMessage = null;
			for (int i = 0; i < 10; i++) {
				String deathMessage = dmg.generateMessage("Beard_Rinker", "Beard_Rinker " + defaultMessage);
				log.info("     Death Message: " + deathMessage);
				assertFalse(deathMessage.equals(lastMessage));
				lastMessage = deathMessage;
			}
		}
	}

}
