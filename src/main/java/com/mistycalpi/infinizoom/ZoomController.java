package com.mistycalpi.infinizoom;

import com.mistycalpi.infinizoom.config.ConfigManager;
import com.mistycalpi.infinizoom.config.ZoomConfig;
import net.minecraft.client.Minecraft;

public final class ZoomController {

    private static boolean zooming = false;
    private static double targetFovFactor = 1.0;
    private static double currentMultiplier = 1.0;
    private static long lastUpdateNanos = 0L;
    private static double lastBaseFov = 70.0;

    private static final double WIDE_REGION_STEP_SCALE = 0.2;

    private ZoomController() {}

    public static void startZoom() {
        zooming = true;
        targetFovFactor = clamp(1.0 / config().defaultZoom, 1.0e-6, 1.0);
    }

    public static void setZooming(boolean held) {
        zooming = held;
    }

    public static boolean isZooming() {
        return zooming;
    }

    public static void onScroll(double amount) {
        ZoomConfig cfg = config();
        double dir = cfg.invertScroll ? -amount : amount;

        double step = cfg.scrollStep;
        boolean inWideRegion = targetFovFactor > 1.0
                || (targetFovFactor >= 1.0 - 1.0e-9 && dir < 0);
        if (inWideRegion) {
            step *= WIDE_REGION_STEP_SCALE;
        }

        double next = targetFovFactor * Math.pow(1.0 + step, -dir);

        if ((targetFovFactor < 1.0 && next > 1.0) || (targetFovFactor > 1.0 && next < 1.0)) {
            next = 1.0;
        }

        double maxFactor = (lastBaseFov > 0.0) ? cfg.maxZoomOutFov / lastBaseFov : Double.MAX_VALUE;
        maxFactor = Math.max(1.0, maxFactor);
        double minFactor = (cfg.maxZoomIn > 0.0) ? 1.0 / cfg.maxZoomIn : 0.0;
        targetFovFactor = clamp(next, minFactor, maxFactor);
    }

    public static double getFovMultiplier() {
        update();
        return currentMultiplier;
    }

    public static float computeFov(float baseFov) {
        lastBaseFov = baseFov;
        double fov = baseFov * getFovMultiplier();
        double maxFov = config().maxZoomOutFov;
        if (fov > maxFov) {
            fov = maxFov;
        }
        if (!Double.isFinite(fov) || fov <= 0.0) {
            return Float.MIN_VALUE;
        }
        return (float) fov;
    }

    public static double getSensitivityScale() {
        if (currentMultiplier > 0.999) {
            return 1.0;
        }
        ZoomConfig cfg = config();
        double scale = cfg.sensitivityMode.scale(currentMultiplier) * cfg.sensitivityMultiplier;
        return clamp(scale, 1.0e-7, 1.0);
    }

    public static boolean shouldHideHud() {
        ZoomConfig cfg = config();
        if (!cfg.scaleGuiWithZoom) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.screen != null) {
            return false;
        }
        return currentMultiplier < 0.999;
    }

    public static double getHandScaleFactor() {
        ZoomConfig cfg = config();
        if (!cfg.scaleHandWithZoom) {
            return 1.0;
        }
        if (currentMultiplier >= 0.999) {
            return 1.0;
        }
        double factor = Math.sqrt(currentMultiplier);
        return clamp(factor, cfg.minHandScale, 1.0);
    }

    private static void update() {
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
        }
        double dt = (now - lastUpdateNanos) / 1.0e9;
        lastUpdateNanos = now;
        dt = clamp(dt, 0.0, 0.1);

        double target = zooming ? targetFovFactor : 1.0;

        double smoothness = config().smoothness;
        if (smoothness <= 0.0) {
            currentMultiplier = target;
            return;
        }
        double speed = (1.0 - smoothness) * 18.0 + 1.5;
        double t = 1.0 - Math.exp(-speed * dt);

        double curLog = Math.log(currentMultiplier);
        double tgtLog = Math.log(target);
        curLog += (tgtLog - curLog) * t;
        currentMultiplier = Math.exp(curLog);

        if (Math.abs(tgtLog - curLog) < 1.0e-4) {
            currentMultiplier = target;
        }
    }

    private static ZoomConfig config() {
        return ConfigManager.get();
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
