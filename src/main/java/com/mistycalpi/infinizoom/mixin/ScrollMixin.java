package com.mistycalpi.infinizoom.mixin;

import com.mistycalpi.infinizoom.ZoomController;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class ScrollMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void infinizoom$captureScroll(long window, double xScroll, double yScroll, CallbackInfo ci) {
        if (ZoomController.isZooming()) {
            double delta = (yScroll != 0.0) ? yScroll : xScroll;
            if (delta != 0.0) {
                ZoomController.onScroll(delta);
                ci.cancel();
            }
        }
    }
}
