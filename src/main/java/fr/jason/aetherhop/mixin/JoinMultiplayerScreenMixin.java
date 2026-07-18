package fr.jason.aetherhop.mixin;

import fr.jason.aetherhop.gui.ProxyConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds a button to the server-list screen that opens the AetherHop config directly. */
@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {

    private JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void aetherHop$addButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.translatable("aetherhop.button"),
                button -> {
                    //? if <26.2 {
                    /*Minecraft.getInstance().setScreen(ProxyConfigScreen.create(this));
                    *///? } else {
                    Minecraft.getInstance().setScreenAndShow(ProxyConfigScreen.create(this));
                    //? }
                })
            .bounds(8, 8, 60, 20)
            .build());
    }
}
