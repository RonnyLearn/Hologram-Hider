package net.example.hider;

import net.fabricmc.api.ClientModInitializer;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundCategory;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.Sound;
import net.minecraft.sound.Sounds;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HologramHiderMod implements ClientModInitializer {
	public static final String MOD_ID = "hologram_hider";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static ModConfig config;
	private static final String KEY_CATEGORY = "key.categories.hologram_hider";
	private static final KeyBinding TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.hologram_hider.toggle", Keyboard.KEY_H, KEY_CATEGORY));
	private static final KeyBinding TEMPORARY_SHOW_KEY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.hologram_hider.temporary_show", Keyboard.KEY_LMENU, KEY_CATEGORY));
	private static final KeyBinding TOGGLE_MOBS_KEY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.hologram_hider.toggle_mobs", Keyboard.KEY_J, KEY_CATEGORY));
	private static final Set<UUID> HIDDEN_STANDS_SEEN = new HashSet<>();
	private static final double NEARBY_RADIUS = 32.0;
	private static final float SOUND_VOLUME = 0.125F;
	private static final String OFFICIAL_PAGE = "https://modrinth.com/mod/hologram-hider";
	private static final String WARNING_LINE = new String(new char[40]).replace('\0', '\u2501');
	private static int hiddenStandsNow;
	private static OfficialSource officialSource;

	public static boolean isEnabled() {
		return config != null && config.isEnabled();
	}

	public static boolean shouldHideHolograms() {
		return isEnabled() != TEMPORARY_SHOW_KEY.isPressed();
	}

	public static boolean shouldHideMobNames() {
		return config != null && !TEMPORARY_SHOW_KEY.isPressed()
			&& (config.isHideMobNames() || config.isEnabled() && config.isMainHidesMobNames());
	}

	public static boolean handleCommand(String command) {
		String cmd = command.trim();

		if (cmd.equalsIgnoreCase("hl toggle")) {
			toggle();
			return true;
		}
		if (cmd.equalsIgnoreCase("hl info")) {
			sendFeedback(new TranslatableText("message.hologram_hider.info",
				stateComponent(shouldHideHolograms()), hiddenStandsNow, config.getTotalHiddenStands(),
				stateComponent(config.isSoundsEnabled()), stateComponent(config.isHideMobNames()),
				stateComponent(config.isHideDisplays())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl mobs")) {
			toggleMobNames();
			return true;
		}
		if (cmd.equalsIgnoreCase("hl main")) {
			sendFeedback(new TranslatableText("message.hologram_hider.main",
				stateComponent(config.isMainHidesMobNames()), stateComponent(config.isMainHidesDisplays())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl main mobs")) {
			config.setMainHidesMobNames(!config.isMainHidesMobNames());
			config.save();
			sendFeedback(new TranslatableText("message.hologram_hider.main.mobs", stateComponent(config.isMainHidesMobNames())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl sound")) {
			config.setSoundsEnabled(!config.isSoundsEnabled());
			config.save();
			sendFeedback(new TranslatableText("message.hologram_hider.sound", stateComponent(config.isSoundsEnabled())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl help")) {
			sendFeedback(new TranslatableText("message.hologram_hider.help.header"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.toggle"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.mobs"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.main"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.info"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.sound"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.help"));
			sendFeedback(new TranslatableText("message.hologram_hider.help.nearby"));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl nearby")) {
			sendFeedback(new TranslatableText("message.hologram_hider.nearby", (int) NEARBY_RADIUS, countNearbyHolograms()));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl hidewarning")) {
			if (officialSource != null) {
				officialSource.dismiss();
			}
			sendFeedback(styled(new TranslatableText("message.hologram_hider.source.hidden"), new Style().setFormatting(Formatting.GRAY)));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl")) {
			sendFeedback(new TranslatableText("message.hologram_hider.usage", stateComponent(shouldHideHolograms())));
			return true;
		}

		return false;
	}

	private static void toggle() {
		setEnabled(!config.isEnabled());
	}

	private static void setEnabled(boolean enabled) {
		config.setEnabled(enabled);
		config.save();
		sendFeedback(new TranslatableText("message.hologram_hider.toggle", stateComponent(enabled)));
		playToggleSound(enabled);
	}

	private static void toggleMobNames() {
		boolean hide = !config.isHideMobNames();
		config.setHideMobNames(hide);
		config.save();
		sendFeedback(new TranslatableText("message.hologram_hider.mobs", stateComponent(hide)));
		playToggleSound(hide);
	}

	private static void playToggleSound(boolean enabled) {
		if (config.isSoundsEnabled()) {
			playLocalSound(enabled ? Sounds.ENTITY_EXPERIENCE_ORB_PICKUP : Sounds.BLOCK_ANVIL_BREAK);
		}
	}

	private static Text stateComponent(boolean enabled) {
		return new TranslatableText(enabled ? "message.hologram_hider.on" : "message.hologram_hider.off");
	}

	private static void sendFeedback(Text message) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(message, false);
		}
	}

	private static void playLocalSound(Sound sound) {
		MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(sound.getId(), SoundCategory.MASTER,
			SOUND_VOLUME, 1.0F, false, 0, SoundInstance.AttenuationType.NONE, 0.0F, 0.0F, 0.0F));
	}

	@Override
	public void onInitializeClient() {
		config = ModConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_KEY.wasPressed()) {
				toggle();
			}
			while (TOGGLE_MOBS_KEY.wasPressed()) {
				toggleMobNames();
			}
			updateHiddenStandCounts(client);
			tickOfficialSource(client);
		});
		LOGGER.info("[Hologram Hider] Initialized. Use /hl toggle or the configured key to switch. Current state: " + (config.isEnabled() ? "ON" : "OFF"));
	}

	private static void tickOfficialSource(MinecraftClient client) {
		if (officialSource == null) {
			officialSource = new OfficialSource(ModConfig.directory(), MOD_ID, "net/example/hider/HologramHiderMod.class", "modrinth.com");
		}
		officialSource.tick(client.player != null, HologramHiderMod::showOfficialSourceWarning);
	}

	private static void showOfficialSourceWarning() {
		sendFeedback(styled(new LiteralText(WARNING_LINE), new Style().setFormatting(Formatting.DARK_RED)));
		sendFeedback(styled(new TranslatableText("message.hologram_hider.source.title"), new Style().setFormatting(Formatting.RED).setBold(true)));
		sendFeedback(styled(new TranslatableText("message.hologram_hider.source.danger"), new Style().setFormatting(Formatting.RED)));
		sendFeedback(styled(new TranslatableText("message.hologram_hider.source.support"), new Style().setFormatting(Formatting.GOLD)));
		sendFeedback(styled(new TranslatableText("message.hologram_hider.source.link"), new Style().setFormatting(Formatting.AQUA).setUnderline(true)
			.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, OFFICIAL_PAGE))
			.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(OFFICIAL_PAGE)))));
		sendFeedback(styled(new TranslatableText("message.hologram_hider.source.hide"), new Style().setFormatting(Formatting.GRAY)
			.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hl hidewarning"))
			.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("message.hologram_hider.source.hide.hover")))));
		sendFeedback(styled(new LiteralText(WARNING_LINE), new Style().setFormatting(Formatting.DARK_RED)));
		playLocalSound(Sounds.BLOCK_NOTE_BASS);
	}

	private static Text styled(Text text, Style style) {
		text.setStyle(style);
		return text;
	}
	private static void updateHiddenStandCounts(MinecraftClient client) {
		hiddenStandsNow = 0;
		if (!shouldHideHolograms() || client.world == null) {
			return;
		}

		for (Entity entity : client.world.loadedEntities) {
			if (entity instanceof ArmorStandEntity && entity.hasCustomName() && entity.isCustomNameVisible()) {
				hiddenStandsNow++;
				if (HIDDEN_STANDS_SEEN.add(entity.getUuid())) {
					config.incrementTotalHiddenStands();
					config.save();
				}
			}
		}
	}

	private static int countNearbyHolograms() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null) {
			return 0;
		}

		double radiusSquared = NEARBY_RADIUS * NEARBY_RADIUS;
		int count = 0;
		for (Entity entity : client.world.loadedEntities) {
			if (entity instanceof ArmorStandEntity && entity.hasCustomName() && entity.isCustomNameVisible()
				&& entity.squaredDistanceTo(client.player) <= radiusSquared) {
				count++;
			}
		}
		return count;
	}
}
