package com.mistycalpi.infinizoom.mixin;

import com.mistycalpi.infinizoom.ZoomController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudHideMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void infinizoom$hideHud(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker,
                                    CallbackInfo ci) {
        if (ZoomController.shouldHideHud()) {
            ci.cancel();
        }
    }
}
