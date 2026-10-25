package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandRenderer.class)
public class ArmorStandTextMixin {
	@Inject(
		method = "shouldShowName(Lnet/minecraft/entity/item/ArmorStandEntity;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hologramHider$hideName(ArmorStandEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (HologramHiderMod.shouldHideHolograms()) {
			cir.setReturnValue(false);
		}
	}
}
