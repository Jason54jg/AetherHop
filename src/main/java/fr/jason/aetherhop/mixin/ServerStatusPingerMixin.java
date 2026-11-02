package fr.jason.aetherhop.mixin;

import fr.jason.aetherhop.AetherHop;
import fr.jason.aetherhop.config.ProxyConfig;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The legacy status fallback uses its own Bootstrap and bypasses Connection's proxy handler. */
@Mixin(ServerStatusPinger.class)
public abstract class ServerStatusPingerMixin {
    @Inject(method = "pingLegacyServer", at = @At("HEAD"), cancellable = true)
    private void aetherHop$blockDirectFallback(CallbackInfo ci) {
        ProxyConfig config = AetherHop.config();
        if (config == null || config.enabled || config.hasLoadError()) {
            ci.cancel();
        }
    }
}
