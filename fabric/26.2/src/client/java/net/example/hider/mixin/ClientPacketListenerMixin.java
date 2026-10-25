package net.example.hider.mixin;

import net.example.hider.HologramHiderMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleCommands", at = @At("RETURN"))
	private void hologramHider$onCommands(ClientboundCommandsPacket packet, CallbackInfo ci) {
		HologramHiderMod.registerClientSuggestions(((ClientPacketListener) (Object) this).getCommands());
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void hologramHider$onSendCommand(String command, CallbackInfo ci) {
		if (HologramHiderMod.handleCommand(command)) {
			ci.cancel();
		}
	}

	@Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
	private void hologramHider$onSendUnattendedCommand(String command, Screen screen, CallbackInfo ci) {
		if (HologramHiderMod.handleCommand(command)) {
			ci.cancel();
		}
	}
}
