package net.example.hider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod(modid = HologramHiderMod.MOD_ID, name = "Hologram Hider", version = "1.4.8", clientSideOnly = true,
	acceptedMinecraftVersions = "[1.12.2]")
public class HologramHiderMod {
	public static final String MOD_ID = "hologram_hider";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static ModConfig config;
	private static final String KEY_CATEGORY = "key.categories.hologram_hider";
	private static final KeyBinding TOGGLE_KEY = new KeyBinding("key.hologram_hider.toggle", Keyboard.KEY_H, KEY_CATEGORY);
	private static final KeyBinding TEMPORARY_SHOW_KEY = new KeyBinding("key.hologram_hider.temporary_show", Keyboard.KEY_LMENU, KEY_CATEGORY);
	private static final KeyBinding TOGGLE_MOBS_KEY = new KeyBinding("key.hologram_hider.toggle_mobs", Keyboard.KEY_J, KEY_CATEGORY);
	private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");
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
		return isEnabled() != TEMPORARY_SHOW_KEY.isKeyDown();
	}

	public static boolean shouldHideMobNames() {
		return config != null && !TEMPORARY_SHOW_KEY.isKeyDown()
			&& (config.isHideMobNames() || config.isEnabled() && config.isMainHidesMobNames());
	}

	public static boolean handleCommand(String command) {
		String cmd = command.trim();

		if (cmd.equalsIgnoreCase("hl toggle")) {
			toggle();
			return true;
		}
		if (cmd.equalsIgnoreCase("hl info")) {
			sendFeedback(new TextComponentTranslation("message.hologram_hider.info",
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
			sendFeedback(new TextComponentTranslation("message.hologram_hider.main",
				stateComponent(config.isMainHidesMobNames()), stateComponent(config.isMainHidesDisplays())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl main mobs")) {
			config.setMainHidesMobNames(!config.isMainHidesMobNames());
			config.save();
			sendFeedback(new TextComponentTranslation("message.hologram_hider.main.mobs", stateComponent(config.isMainHidesMobNames())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl sound")) {
			config.setSoundsEnabled(!config.isSoundsEnabled());
			config.save();
			sendFeedback(new TextComponentTranslation("message.hologram_hider.sound", stateComponent(config.isSoundsEnabled())));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl help")) {
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.header"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.toggle"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.mobs"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.main"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.info"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.sound"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.help"));
			sendFeedback(new TextComponentTranslation("message.hologram_hider.help.nearby"));
			return true;
		}
		if (cmd.equalsIgnoreCase("hl nearby")) {
			sendFeedback(new TextComponentTranslation("message.hologram_hider.nearby", (int) NEARBY_RADIUS, countNearbyHolograms()));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl hidewarning")) {
			if (officialSource != null) {
				officialSource.dismiss();
			}
			sendFeedback(styled(new TextComponentTranslation("message.hologram_hider.source.hidden"), new Style().setColor(TextFormatting.GRAY)));
			return true;
		}

		if (cmd.equalsIgnoreCase("hl")) {
			sendFeedback(new TextComponentTranslation("message.hologram_hider.usage", stateComponent(shouldHideHolograms())));
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
		sendFeedback(new TextComponentTranslation("message.hologram_hider.toggle", stateComponent(enabled)));
		playToggleSound(enabled);
	}

	private static void toggleMobNames() {
		boolean hide = !config.isHideMobNames();
		config.setHideMobNames(hide);
		config.save();
		sendFeedback(new TextComponentTranslation("message.hologram_hider.mobs", stateComponent(hide)));
		playToggleSound(hide);
	}

	private static void playToggleSound(boolean enabled) {
		if (config.isSoundsEnabled()) {
			playLocalSound(enabled ? SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP : SoundEvents.BLOCK_ANVIL_BREAK);
		}
	}

	private static ITextComponent stateComponent(boolean enabled) {
		return new TextComponentTranslation(enabled ? "message.hologram_hider.on" : "message.hologram_hider.off");
	}

	private static void sendFeedback(ITextComponent message) {
		Minecraft client = Minecraft.getMinecraft();
		if (client.player != null) {
			client.player.sendStatusMessage(message, false);
		}
	}

	private static void playLocalSound(SoundEvent sound) {
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound.getSoundName(), SoundCategory.MASTER,
			SOUND_VOLUME, 1.0F, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		config = ModConfig.load();
		ClientRegistry.registerKeyBinding(TOGGLE_KEY);
		ClientRegistry.registerKeyBinding(TEMPORARY_SHOW_KEY);
		ClientRegistry.registerKeyBinding(TOGGLE_MOBS_KEY);
		ClientCommandHandler.instance.registerCommand(new HlCommand());
		MinecraftForge.EVENT_BUS.register(this);
		LOGGER.info("[Hologram Hider] Initialized. Use /hl toggle or the configured key to switch. Current state: " + (config.isEnabled() ? "ON" : "OFF"));
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		Minecraft client = Minecraft.getMinecraft();
		while (TOGGLE_KEY.isPressed()) {
			toggle();
		}
		while (TOGGLE_MOBS_KEY.isPressed()) {
			toggleMobNames();
		}
		updateHiddenStandCounts(client);
		tickOfficialSource(client);
	}

	@SubscribeEvent
	public void onRenderName(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
		EntityLivingBase entity = event.getEntity();
		if (!entity.hasCustomName()) {
			return;
		}
		if (entity instanceof EntityArmorStand) {
			if (shouldHideHolograms()) {
				event.setCanceled(true);
			}
		} else if (entity instanceof EntityLiving && shouldHideMobNames()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		Minecraft client = Minecraft.getMinecraft();
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !shouldHideHolograms() || client.player == null) {
			return;
		}

		int x = event.getResolution().getScaledWidth() - 24;
		int y = 2 + client.player.getActivePotionEffects().size() * 26;
		Gui.drawRect(x, y, x + 22, y + 22, 0xAA202020);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		client.getTextureManager().bindTexture(INVENTORY_TEXTURE);
		int icon = MobEffects.BLINDNESS.getStatusIconIndex();
		client.ingameGUI.drawTexturedModalRect(x + 2, y + 2, icon % 8 * 18, 198 + icon / 8 * 18, 18, 18);
	}

	private static void tickOfficialSource(Minecraft client) {
		if (officialSource == null) {
			officialSource = new OfficialSource(ModConfig.directory(), MOD_ID, "net/example/hider/HologramHiderMod.class", "modrinth.com");
		}
		officialSource.tick(client.player != null, HologramHiderMod::showOfficialSourceWarning);
	}

	private static void showOfficialSourceWarning() {
		sendFeedback(styled(new TextComponentString(WARNING_LINE), new Style().setColor(TextFormatting.DARK_RED)));
		sendFeedback(styled(new TextComponentTranslation("message.hologram_hider.source.title"), new Style().setColor(TextFormatting.RED).setBold(true)));
		sendFeedback(styled(new TextComponentTranslation("message.hologram_hider.source.danger"), new Style().setColor(TextFormatting.RED)));
		sendFeedback(styled(new TextComponentTranslation("message.hologram_hider.source.support"), new Style().setColor(TextFormatting.GOLD)));
		sendFeedback(styled(new TextComponentTranslation("message.hologram_hider.source.link"), new Style().setColor(TextFormatting.AQUA).setUnderlined(true)
			.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, OFFICIAL_PAGE))
			.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(OFFICIAL_PAGE)))));
		sendFeedback(styled(new TextComponentTranslation("message.hologram_hider.source.hide"), new Style().setColor(TextFormatting.GRAY)
			.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hl hidewarning"))
			.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("message.hologram_hider.source.hide.hover")))));
		sendFeedback(styled(new TextComponentString(WARNING_LINE), new Style().setColor(TextFormatting.DARK_RED)));
		playLocalSound(SoundEvents.BLOCK_NOTE_BASS);
	}

	private static ITextComponent styled(ITextComponent text, Style style) {
		text.setStyle(style);
		return text;
	}
	private static void updateHiddenStandCounts(Minecraft client) {
		hiddenStandsNow = 0;
		if (!shouldHideHolograms() || client.world == null) {
			return;
		}

		for (Entity entity : client.world.loadedEntityList) {
			if (entity instanceof EntityArmorStand && entity.hasCustomName() && entity.getAlwaysRenderNameTag()) {
				hiddenStandsNow++;
				if (HIDDEN_STANDS_SEEN.add(entity.getUniqueID())) {
					config.incrementTotalHiddenStands();
					config.save();
				}
			}
		}
	}

	private static int countNearbyHolograms() {
		Minecraft client = Minecraft.getMinecraft();
		if (client.world == null || client.player == null) {
			return 0;
		}

		double radiusSquared = NEARBY_RADIUS * NEARBY_RADIUS;
		int count = 0;
		for (Entity entity : client.world.loadedEntityList) {
			if (entity instanceof EntityArmorStand && entity.hasCustomName() && entity.getAlwaysRenderNameTag()
				&& entity.getDistanceSq(client.player) <= radiusSquared) {
				count++;
			}
		}
		return count;
	}

	private static final class HlCommand extends CommandBase {
		@Override
		public String getName() {
			return "hl";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "/hl <toggle|info|mobs|main [mobs]|sound|help|nearby>";
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 0;
		}

		@Override
		public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
			return true;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
			if (!handleCommand(("hl " + String.join(" ", args)).trim())) {
				handleCommand("hl");
			}
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			if (args.length == 1) {
				return getListOfStringsMatchingLastWord(args, "toggle", "info", "mobs", "main", "sound", "help", "nearby");
			}
			if (args.length == 2 && args[0].equalsIgnoreCase("main")) {
				return getListOfStringsMatchingLastWord(args, "mobs");
			}
			return Collections.emptyList();
		}
	}
}
