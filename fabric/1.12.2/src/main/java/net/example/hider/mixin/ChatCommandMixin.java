package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ChatCommandMixin {
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void hologramHider$onChat(String message, CallbackInfo ci) {
		if (message.startsWith("/") && HologramHiderMod.handleCommand(message.substring(1))) {
			ci.cancel();
		}
	}
}
