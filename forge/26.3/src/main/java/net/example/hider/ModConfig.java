package net.example.hider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("hologram_hider.json").toFile();

	static File directory() {
		return CONFIG_FILE.getParentFile();
	}

	private boolean enabled = true;
	private boolean soundsEnabled = true;
	private boolean hideMobNames;
	private boolean hideDisplays;
	private boolean mainHidesMobNames;
	private boolean mainHidesDisplays;
	private long totalHiddenStands;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSoundsEnabled() {
		return soundsEnabled;
	}

	public void setSoundsEnabled(boolean soundsEnabled) {
		this.soundsEnabled = soundsEnabled;
	}

	public boolean isHideMobNames() {
		return hideMobNames;
	}

	public void setHideMobNames(boolean hideMobNames) {
		this.hideMobNames = hideMobNames;
	}

	public boolean isHideDisplays() {
		return hideDisplays;
	}

	public void setHideDisplays(boolean hideDisplays) {
		this.hideDisplays = hideDisplays;
	}

	public boolean isMainHidesMobNames() {
		return mainHidesMobNames;
	}

	public void setMainHidesMobNames(boolean mainHidesMobNames) {
		this.mainHidesMobNames = mainHidesMobNames;
	}

	public boolean isMainHidesDisplays() {
		return mainHidesDisplays;
	}

	public void setMainHidesDisplays(boolean mainHidesDisplays) {
		this.mainHidesDisplays = mainHidesDisplays;
	}

	public long getTotalHiddenStands() {
		return totalHiddenStands;
	}

	public void incrementTotalHiddenStands() {
		totalHiddenStands++;
	}

	public static ModConfig load() {
		if (!CONFIG_FILE.exists()) {
			ModConfig config = new ModConfig();
			config.save();
			return config;
		}

		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			return GSON.fromJson(reader, ModConfig.class);
		} catch (IOException e) {
			HologramHiderMod.LOGGER.error("[Hologram Hider] Failed to load config", e);
			return new ModConfig();
		}
	}

	public void save() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			HologramHiderMod.LOGGER.error("[Hologram Hider] Failed to save config", e);
		}
	}
}
