package com.mistycalpi.infinizoom.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mistycalpi.infinizoom.ZoomController;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraFovMixin {

    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float infinizoom$applyZoom(float originalFov) {
        return ZoomController.computeFov(originalFov);
    }
}
