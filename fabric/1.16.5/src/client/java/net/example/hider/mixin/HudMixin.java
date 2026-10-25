package net.example.hider.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.example.hider.HologramHiderMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
	private void hologramHider$renderFakeBlindnessEffect(PoseStack poseStack, CallbackInfo ci) {
		if (!HologramHiderMod.shouldHideHolograms() || minecraft.player == null) {
			return;
		}

		int x = minecraft.getWindow().getGuiScaledWidth() - 24;
		int y = 2 + minecraft.player.getActiveEffects().size() * 26;
		GuiComponent.fill(poseStack, x, y, x + 22, y + 22, 0xAA202020);
		TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(MobEffects.BLINDNESS);
		minecraft.getTextureManager().bind(sprite.atlas().location());
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, x + 2, y + 2, 0, 18, 18, sprite);
	}
}
