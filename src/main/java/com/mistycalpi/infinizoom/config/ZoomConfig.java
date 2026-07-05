package com.mistycalpi.infinizoom.config;

public class ZoomConfig {

    public double smoothness = 0.65;
    public double scrollStep = 0.5;
    public double defaultZoom = 4.0;
    public double maxZoomOutFov = 179.0;
    public SensitivityMode sensitivityMode = SensitivityMode.PROPORTIONAL;
    public double sensitivityMultiplier = 1.0;
    public boolean invertScroll = false;
    public boolean toggleMode = false;
    public boolean scaleGuiWithZoom = true;
    public boolean scaleHandWithZoom = true;
    public double minHandScale = 0.15;
    public double maxZoomIn = 0.0;

    public void copyFrom(ZoomConfig other) {
        this.smoothness = other.smoothness;
        this.scrollStep = other.scrollStep;
        this.defaultZoom = other.defaultZoom;
        this.maxZoomOutFov = other.maxZoomOutFov;
        this.sensitivityMode = other.sensitivityMode;
        this.sensitivityMultiplier = other.sensitivityMultiplier;
        this.invertScroll = other.invertScroll;
        this.toggleMode = other.toggleMode;
        this.scaleGuiWithZoom = other.scaleGuiWithZoom;
        this.scaleHandWithZoom = other.scaleHandWithZoom;
        this.minHandScale = other.minHandScale;
        this.maxZoomIn = other.maxZoomIn;
    }

    public void clampToValidRanges() {
        smoothness = clamp(smoothness, 0.0, 1.0);
        scrollStep = clamp(scrollStep, 0.05, 2.0);
        defaultZoom = clamp(defaultZoom, 1.0, 50.0);
        maxZoomOutFov = clamp(maxZoomOutFov, 110.0, 179.0);
        sensitivityMultiplier = clamp(sensitivityMultiplier, 0.1, 3.0);
        minHandScale = clamp(minHandScale, 0.05, 1.0);
        if (maxZoomIn > 0.0) {
            maxZoomIn = clamp(maxZoomIn, 10.0, 1.0e12);
        }
        if (sensitivityMode == null) {
            sensitivityMode = SensitivityMode.PROPORTIONAL;
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
