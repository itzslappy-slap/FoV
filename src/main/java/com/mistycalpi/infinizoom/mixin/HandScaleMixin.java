package com.mistycalpi.infinizoom.mixin;

import com.mistycalpi.infinizoom.ZoomController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HandScaleMixin {

    @Inject(method = "applyItemArmTransform", at = @At("RETURN"))
    private void infinizoom$shrinkHeldItem(PoseStack poseStack, HumanoidArm arm,
                                           float equipProgress, CallbackInfo ci) {
        infinizoom$scale(poseStack);
    }

    @Inject(
            method = "renderPlayerArm",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void infinizoom$shrinkBareArm(PoseStack poseStack, SubmitNodeCollector collector,
                                          int light, float equipProgress, float swingProgress,
                                          HumanoidArm arm, CallbackInfo ci) {
        infinizoom$scale(poseStack);
    }

    private static void infinizoom$scale(PoseStack poseStack) {
        double s = ZoomController.getHandScaleFactor();
        if (s != 1.0) {
            float f = (float) s;
            poseStack.scale(f, f, f);
        }
    }
}
