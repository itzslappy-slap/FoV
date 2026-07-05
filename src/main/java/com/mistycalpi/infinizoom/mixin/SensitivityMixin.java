package com.mistycalpi.infinizoom.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mistycalpi.infinizoom.ZoomController;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class SensitivityMixin {

    private static double infinizoom$carryYaw = 0.0;
    private static double infinizoom$carryPitch = 0.0;

    private static final double TURN_FACTOR = 0.15;

    @WrapOperation(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    private void infinizoom$scaledTurn(LocalPlayer player, double yaw, double pitch,
                                       Operation<Void> original) {
        double scale = ZoomController.getSensitivityScale();
        if (scale >= 1.0) {
            infinizoom$carryYaw = 0.0;
            infinizoom$carryPitch = 0.0;
            original.call(player, yaw, pitch);
            return;
        }

        double desiredYaw = yaw * scale + infinizoom$carryYaw;
        double appliedYaw = infinizoom$flush(desiredYaw, player.getYRot());
        infinizoom$carryYaw = desiredYaw - appliedYaw;

        double desiredPitch = pitch * scale + infinizoom$carryPitch;
        double appliedPitch = infinizoom$flush(desiredPitch, player.getXRot());
        infinizoom$carryPitch = desiredPitch - appliedPitch;

        original.call(player, appliedYaw, appliedPitch);
    }

    private static double infinizoom$flush(double desiredWorldDelta, float currentRot) {
        double rotDelta = desiredWorldDelta * TURN_FACTOR;
        double ulp = Math.ulp(currentRot);
        if (Math.abs(rotDelta) < ulp) {
            return 0.0;
        }
        double steps = (rotDelta > 0.0) ? Math.floor(rotDelta / ulp) : Math.ceil(rotDelta / ulp);
        return (steps * ulp) / TURN_FACTOR;
    }
}
