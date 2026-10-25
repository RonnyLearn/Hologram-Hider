package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class TextDisplayMixin {
	@Inject(
		method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDDF)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hologramHider$hideTextDisplay(Entity entity, Frustum frustum, double camX, double camY, double camZ, float partialTick, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof Display.TextDisplay && HologramHiderMod.shouldHideDisplays()) {
			cir.setReturnValue(false);
		}
	}
}
