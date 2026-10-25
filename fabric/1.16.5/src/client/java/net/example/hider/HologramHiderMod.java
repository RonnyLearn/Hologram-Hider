package net.example.hider;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HologramHiderMod implements ClientModInitializer {
	public static final String MOD_ID = "hologram_hider";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static ModConfig config;
	private static final String KEY_CATEGORY = "key.categories.hologram_hider";
	private static final KeyMapping TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.hologram_hider.toggle",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_H,
		KEY_CATEGORY
	));
	private static final KeyMapping TEMPORARY_SHOW_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.hologram_hider.temporary_show",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT_ALT,
		KEY_CATEGORY
	));
	private static final KeyMapping TOGGLE_MOBS_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.hologram_hider.toggle_mobs",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_J,
		KEY_CATEGORY
	));

	private static final Set<UUID> HIDDEN_STANDS_SEEN = new HashSet<>();
	private static final double NEARBY_RADIUS = 32.0;
	private static final float SOUND_VOLUME = 0.125F;
	private static final String OFFICIAL_PAGE = "https://modrinth.com/mod/hologram-hider";
	private static final String WARNING_LINE = new String(new char[40]).replace('\0', '━');
	private static int hiddenStandsNow;
	private static OfficialSource officialSource;

	public static boolean isEnabled() {
		return config != null && config.isEnabled();
	}

	public static boolean shouldHideHolograms() {
		return isEnabled() != TEMPORARY_SHOW_KEY.isDown();
	}

	public static boolean shouldHideMobNames() {
		return config != null && !TEMPORARY_SHOW_KEY.isDown()
			&& (config.isHideMobNames() || config.isEnabled() && config.isMainHidesMobNames());
	}

	public static boolean shouldHideDisplays() {
		return config != null && !TEMPORARY_SHOW_KEY.isDown()
			&& (config.isHideDisplays() || config.isEnabled() && config.isMainHidesDisplays());
	}

	public static void registerClientSuggestions(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<SharedSuggestionProvider>literal("hl")
				.executes(ctx -> Command.SINGLE_SUCCESS)
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("toggle")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("info")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("sound")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("help")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("nearby")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("mobs")
					.executes(ctx -> Command.SINGLE_SUCCESS))

				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("main")
					.executes(ctx -> Command.SINGLE_SUCCESS)
					.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("mobs")
						.executes(ctx -> Command.SINGLE_SUCCESS))
)
		);
	}

	public static boolean handleCommand(String command) {
		String cmd = command.trim();

		if (cmd.equalsIgnoreCase("hl toggle")) {
			toggle();
			return true;
		}
		if (cmd.equalsIgnoreCase("hl info")) {
			sendFeedback(new TranslatableComponent("message.hologram_hider.info",
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
			sendFeedback(new TranslatableComponent("message.hologram_hider.main",
				stateComponent(config.isMainHidesMobNames()), stateComponent(config.isMainHidesDisplays())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl main mobs")) {
			config.setMainHidesMobNames(!config.isMainHidesMobNames());
			config.save();
			sendFeedback(new TranslatableComponent("message.hologram_hider.main.mobs", stateComponent(config.isMainHidesMobNames())));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl sound")) {
			config.setSoundsEnabled(!config.isSoundsEnabled());
			config.save();
			sendFeedback(new TranslatableComponent("message.hologram_hider.sound", stateComponent(config.isSoundsEnabled())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl help")) {
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.header"));
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.toggle"));
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.mobs"));

			sendFeedback(new TranslatableComponent("message.hologram_hider.help.main"));
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.info"));
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.sound"));
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.help"));
			sendFeedback(new TranslatableComponent("message.hologram_hider.help.nearby"));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl nearby")) {
			sendFeedback(new TranslatableComponent("message.hologram_hider.nearby", (int) NEARBY_RADIUS, countNearbyHolograms()));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl hidewarning")) {
			if (officialSource != null) {
				officialSource.dismiss();
			}
			sendFeedback(new TranslatableComponent("message.hologram_hider.source.hidden").withStyle(ChatFormatting.GRAY));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl")) {
			sendFeedback(new TranslatableComponent("message.hologram_hider.usage", stateComponent(shouldHideHolograms())));
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
		sendFeedback(new TranslatableComponent("message.hologram_hider.toggle", stateComponent(enabled)));
		playToggleSound(enabled);
	}

	private static void toggleMobNames() {
		boolean hide = !config.isHideMobNames();
		config.setHideMobNames(hide);
		config.save();
		sendFeedback(new TranslatableComponent("message.hologram_hider.mobs", stateComponent(hide)));
		playToggleSound(hide);
	}

	private static void toggleDisplays() {
		boolean hide = !config.isHideDisplays();
		config.setHideDisplays(hide);
		config.save();
		sendFeedback(new TranslatableComponent("message.hologram_hider.displays", stateComponent(hide)));
		playToggleSound(hide);
	}

	private static void playToggleSound(boolean enabled) {
		if (config.isSoundsEnabled()) {
			playLocalSound(enabled ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.ANVIL_BREAK);
		}
	}

	private static Component stateComponent(boolean enabled) {
		return new TranslatableComponent(enabled ? "message.hologram_hider.on" : "message.hologram_hider.off");
	}

	private static void sendFeedback(Component message) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.displayClientMessage(message, false);
		}
	}

	private static void playLocalSound(SoundEvent sound) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, SOUND_VOLUME));
	}

	@Override
	public void onInitializeClient() {
		config = ModConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_KEY.consumeClick()) {
				toggle();
			}
			while (TOGGLE_MOBS_KEY.consumeClick()) {
				toggleMobNames();
			}

			updateHiddenStandCounts(client);
			tickOfficialSource(client);
		});
		LOGGER.info("[Hologram Hider] Initialized. Use /hl toggle or the configured key to switch. Current state: " + (config.isEnabled() ? "ON" : "OFF"));
	}

	private static void tickOfficialSource(Minecraft client) {
		if (officialSource == null) {
			officialSource = new OfficialSource(ModConfig.directory(), MOD_ID, "net/example/hider/HologramHiderMod.class", "modrinth.com");
		}
		officialSource.tick(client.player != null, HologramHiderMod::showOfficialSourceWarning);
	}

	private static void showOfficialSourceWarning() {
		sendFeedback(new TextComponent(WARNING_LINE).withStyle(ChatFormatting.DARK_RED));
		sendFeedback(new TranslatableComponent("message.hologram_hider.source.title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
		sendFeedback(new TranslatableComponent("message.hologram_hider.source.danger").withStyle(ChatFormatting.RED));
		sendFeedback(new TranslatableComponent("message.hologram_hider.source.support").withStyle(ChatFormatting.GOLD));
		sendFeedback(new TranslatableComponent("message.hologram_hider.source.link").withStyle(style -> style
			.withColor(ChatFormatting.AQUA).withUnderlined(true)
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, OFFICIAL_PAGE))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(OFFICIAL_PAGE)))));
		sendFeedback(new TranslatableComponent("message.hologram_hider.source.hide").withStyle(style -> style
			.withColor(ChatFormatting.GRAY)
			.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hl hidewarning"))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("message.hologram_hider.source.hide.hover")))));
		sendFeedback(new TextComponent(WARNING_LINE).withStyle(ChatFormatting.DARK_RED));
		playLocalSound(SoundEvents.BEACON_DEACTIVATE);
	}

	private static void updateHiddenStandCounts(Minecraft client) {
		hiddenStandsNow = 0;
		if (!shouldHideHolograms() || client.level == null) {
			return;
		}

		for (Entity entity : client.level.entitiesForRendering()) {
			ArmorStand stand = entity instanceof ArmorStand ? (ArmorStand) entity : null;
			if (stand != null && stand.hasCustomName() && stand.isCustomNameVisible()) {
				hiddenStandsNow++;
				if (HIDDEN_STANDS_SEEN.add(stand.getUUID())) {
					config.incrementTotalHiddenStands();
					config.save();
				}
			}
		}
	}

	private static int countNearbyHolograms() {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) {
			return 0;
		}

		double radiusSquared = NEARBY_RADIUS * NEARBY_RADIUS;
		int count = 0;
		for (Entity entity : client.level.entitiesForRendering()) {
			ArmorStand stand = entity instanceof ArmorStand ? (ArmorStand) entity : null;
			if (stand != null && stand.hasCustomName() && stand.isCustomNameVisible()
				&& stand.distanceToSqr(client.player) <= radiusSquared) {
				count++;
			}
		}
		return count;
	}
}
