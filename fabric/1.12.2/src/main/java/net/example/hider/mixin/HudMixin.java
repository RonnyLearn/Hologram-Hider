package net.example.hider.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.example.hider.HologramHiderMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin {
	private static final Identifier INVENTORY_TEXTURE = new Identifier("textures/gui/container/inventory.png");

	@Inject(method = "method_12165", at = @At("RETURN"))
	private void hologramHider$renderFakeBlindnessEffect(Window window, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!HologramHiderMod.shouldHideHolograms() || client.player == null) {
			return;
		}

		int x = window.getWidth() - 24;
		int y = 2 + client.player.getStatusEffectInstances().size() * 26;
		DrawableHelper.fill(x, y, x + 22, y + 22, 0xAA202020);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		client.getTextureManager().bindTexture(INVENTORY_TEXTURE);
		int icon = StatusEffects.BLINDNESS.getIconLevel();
		((DrawableHelper) (Object) this).drawTexture(x + 2, y + 2, icon % 8 * 18, 198 + icon / 8 * 18, 18, 18);
	}
}
