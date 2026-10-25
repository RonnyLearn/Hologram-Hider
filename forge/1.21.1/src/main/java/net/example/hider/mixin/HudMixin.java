package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "renderEffects", at = @At("RETURN"))
	private void hologramHider$renderFakeBlindnessEffect(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!HologramHiderMod.shouldHideHolograms() || minecraft.player == null) {
			return;
		}

		int x = graphics.guiWidth() - 24;
		int y = 2 + minecraft.player.getActiveEffects().size() * 26;
		graphics.fill(x, y, x + 22, y + 22, 0xAA202020);
		graphics.blit(x + 2, y + 2, 0, 18, 18, minecraft.getMobEffectTextures().get(MobEffects.BLINDNESS));
	}
}
