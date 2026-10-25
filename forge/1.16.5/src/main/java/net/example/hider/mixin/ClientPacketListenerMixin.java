package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SCommandListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleCommands", at = @At("RETURN"))
	private void hologramHider$onCommands(SCommandListPacket packet, CallbackInfo ci) {
		HologramHiderMod.registerClientSuggestions(((ClientPlayNetHandler) (Object) this).getCommands());
	}
}
