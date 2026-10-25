package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntityRenderer.class)
public class ArmorStandTextMixin {
	@Inject(
		method = "hasLabel(Lnet/minecraft/entity/decoration/ArmorStandEntity;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hologramHider$hideName(ArmorStandEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (HologramHiderMod.shouldHideHolograms()) {
			cir.setReturnValue(false);
		}
	}
}
