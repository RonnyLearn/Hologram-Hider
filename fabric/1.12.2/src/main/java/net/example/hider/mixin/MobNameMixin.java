package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntityRenderer.class)
public class MobNameMixin {
	@Inject(
		method = "hasLabel(Lnet/minecraft/entity/mob/MobEntity;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hologramHider$hideMobName(MobEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (HologramHiderMod.shouldHideMobNames()) {
			cir.setReturnValue(false);
		}
	}
}
