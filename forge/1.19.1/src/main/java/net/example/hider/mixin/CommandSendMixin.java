package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class CommandSendMixin {
	@Inject(method = "commandSigned", at = @At("HEAD"), cancellable = true)
	private void hologramHider$onCommandSigned(String command, Component preview, CallbackInfo ci) {
		if (HologramHiderMod.handleCommand(command)) {
			ci.cancel();
		}
	}

	@Inject(method = "commandUnsigned", at = @At("HEAD"), cancellable = true)
	private void hologramHider$onCommandUnsigned(String command, CallbackInfoReturnable<Boolean> cir) {
		if (HologramHiderMod.handleCommand(command)) {
			cir.setReturnValue(true);
		}
	}
}
