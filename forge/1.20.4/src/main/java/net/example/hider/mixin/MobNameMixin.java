package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobRenderer.class)
public class MobNameMixin {
	@Inject(
		method = "shouldShowName(Lnet/minecraft/world/entity/Mob;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hologramHider$hideMobName(Mob entity, CallbackInfoReturnable<Boolean> cir) {
		if (HologramHiderMod.shouldHideMobNames()) {
			cir.setReturnValue(false);
		}
	}
}
