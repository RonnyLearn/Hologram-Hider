package net.example.hider;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod(HologramHiderMod.MOD_ID)
public class HologramHiderMod {
	public static final String MOD_ID = "hologram_hider";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static ModConfig config;
	private static final String KEY_CATEGORY = "key.categories.hologram_hider";
	private static final KeyBinding TOGGLE_KEY = new KeyBinding(
		"key.hologram_hider.toggle",
		InputMappings.Type.KEYSYM,
		GLFW.GLFW_KEY_H,
		KEY_CATEGORY
	);
	private static final KeyBinding TEMPORARY_SHOW_KEY = new KeyBinding(
		"key.hologram_hider.temporary_show",
		InputMappings.Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT_ALT,
		KEY_CATEGORY
	);
	private static final KeyBinding TOGGLE_MOBS_KEY = new KeyBinding(
		"key.hologram_hider.toggle_mobs",
		InputMappings.Type.KEYSYM,
		GLFW.GLFW_KEY_J,
		KEY_CATEGORY
	);

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

	public static void registerClientSuggestions(CommandDispatcher<ISuggestionProvider> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ISuggestionProvider>literal("hl")
				.executes(ctx -> Command.SINGLE_SUCCESS)
				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("toggle")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("info")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("sound")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("help")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("nearby")
					.executes(ctx -> Command.SINGLE_SUCCESS))
				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("mobs")
					.executes(ctx -> Command.SINGLE_SUCCESS))

				.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("main")
					.executes(ctx -> Command.SINGLE_SUCCESS)
					.then(LiteralArgumentBuilder.<ISuggestionProvider>literal("mobs")
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
			sendFeedback(new TranslationTextComponent("message.hologram_hider.info",
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
			sendFeedback(new TranslationTextComponent("message.hologram_hider.main",
				stateComponent(config.isMainHidesMobNames()), stateComponent(config.isMainHidesDisplays())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl main mobs")) {
			config.setMainHidesMobNames(!config.isMainHidesMobNames());
			config.save();
			sendFeedback(new TranslationTextComponent("message.hologram_hider.main.mobs", stateComponent(config.isMainHidesMobNames())));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl sound")) {
			config.setSoundsEnabled(!config.isSoundsEnabled());
			config.save();
			sendFeedback(new TranslationTextComponent("message.hologram_hider.sound", stateComponent(config.isSoundsEnabled())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl help")) {
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.header"));
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.toggle"));
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.mobs"));

			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.main"));
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.info"));
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.sound"));
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.help"));
			sendFeedback(new TranslationTextComponent("message.hologram_hider.help.nearby"));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl nearby")) {
			sendFeedback(new TranslationTextComponent("message.hologram_hider.nearby", (int) NEARBY_RADIUS, countNearbyHolograms()));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl hidewarning")) {
			if (officialSource != null) {
				officialSource.dismiss();
			}
			sendFeedback(new TranslationTextComponent("message.hologram_hider.source.hidden").withStyle(TextFormatting.GRAY));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl")) {
			sendFeedback(new TranslationTextComponent("message.hologram_hider.usage", stateComponent(shouldHideHolograms())));
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
		sendFeedback(new TranslationTextComponent("message.hologram_hider.toggle", stateComponent(enabled)));
		playToggleSound(enabled);
	}

	private static void toggleMobNames() {
		boolean hide = !config.isHideMobNames();
		config.setHideMobNames(hide);
		config.save();
		sendFeedback(new TranslationTextComponent("message.hologram_hider.mobs", stateComponent(hide)));
		playToggleSound(hide);
	}

	private static void toggleDisplays() {
		boolean hide = !config.isHideDisplays();
		config.setHideDisplays(hide);
		config.save();
		sendFeedback(new TranslationTextComponent("message.hologram_hider.displays", stateComponent(hide)));
		playToggleSound(hide);
	}

	private static void playToggleSound(boolean enabled) {
		if (config.isSoundsEnabled()) {
			playLocalSound(enabled ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.ANVIL_BREAK);
		}
	}

	private static ITextComponent stateComponent(boolean enabled) {
		return new TranslationTextComponent(enabled ? "message.hologram_hider.on" : "message.hologram_hider.off");
	}

	private static void sendFeedback(ITextComponent message) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.displayClientMessage(message, false);
		}
	}

	private static void playLocalSound(SoundEvent sound) {
		Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(sound, 1.0F, SOUND_VOLUME));
	}

	public HologramHiderMod() {
		config = ModConfig.load();
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> {
			ClientRegistry.registerKeyBinding(TOGGLE_KEY);
			ClientRegistry.registerKeyBinding(TEMPORARY_SHOW_KEY);
			ClientRegistry.registerKeyBinding(TOGGLE_MOBS_KEY);
		});
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}
			Minecraft client = Minecraft.getInstance();
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
		sendFeedback(new StringTextComponent(WARNING_LINE).withStyle(TextFormatting.DARK_RED));
		sendFeedback(new TranslationTextComponent("message.hologram_hider.source.title").withStyle(TextFormatting.RED, TextFormatting.BOLD));
		sendFeedback(new TranslationTextComponent("message.hologram_hider.source.danger").withStyle(TextFormatting.RED));
		sendFeedback(new TranslationTextComponent("message.hologram_hider.source.support").withStyle(TextFormatting.GOLD));
		sendFeedback(new TranslationTextComponent("message.hologram_hider.source.link").withStyle(style -> style
			.withColor(TextFormatting.AQUA).withUnderlined(true)
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, OFFICIAL_PAGE))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(OFFICIAL_PAGE)))));
		sendFeedback(new TranslationTextComponent("message.hologram_hider.source.hide").withStyle(style -> style
			.withColor(TextFormatting.GRAY)
			.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hl hidewarning"))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("message.hologram_hider.source.hide.hover")))));
		sendFeedback(new StringTextComponent(WARNING_LINE).withStyle(TextFormatting.DARK_RED));
		playLocalSound(SoundEvents.BEACON_DEACTIVATE);
	}

	private static void updateHiddenStandCounts(Minecraft client) {
		hiddenStandsNow = 0;
		if (!shouldHideHolograms() || client.level == null) {
			return;
		}

		for (Entity entity : client.level.entitiesForRendering()) {
			ArmorStandEntity stand = entity instanceof ArmorStandEntity ? (ArmorStandEntity) entity : null;
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
			ArmorStandEntity stand = entity instanceof ArmorStandEntity ? (ArmorStandEntity) entity : null;
			if (stand != null && stand.hasCustomName() && stand.isCustomNameVisible()
				&& stand.distanceToSqr(client.player) <= radiusSquared) {
				count++;
			}
		}
		return count;
	}
}
