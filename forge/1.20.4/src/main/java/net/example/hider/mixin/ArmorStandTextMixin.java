package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandRenderer.class)
public class ArmorStandTextMixin {
	@Inject(
		method = "shouldShowName(Lnet/minecraft/world/entity/decoration/ArmorStand;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hologramHider$hideName(ArmorStand entity, CallbackInfoReturnable<Boolean> cir) {
		if (HologramHiderMod.shouldHideHolograms()) {
			cir.setReturnValue(false);
		}
	}
}
