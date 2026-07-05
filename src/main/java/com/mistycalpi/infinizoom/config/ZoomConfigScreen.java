package com.mistycalpi.infinizoom.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

public class ZoomConfigScreen extends Screen {

    private final Screen parent;
    private final ZoomConfig config = ConfigManager.get();

    public ZoomConfigScreen(Screen parent) {
        super(Component.translatable("infinizoom.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int w = 220;
        int x = centerX - w / 2;
        int y = 30;
        int gap = 24;

        addRenderableWidget(new RangeSlider(x, y, w,
                config.smoothness, 0.0, 1.0,
                v -> String.format("%.2f", v),
                "infinizoom.config.smoothness",
                v -> config.smoothness = v));
        y += gap;

        addRenderableWidget(new RangeSlider(x, y, w,
                config.scrollStep, 0.05, 2.0,
                v -> String.valueOf(Math.round(v * 100)),
                "infinizoom.config.scroll_step",
                v -> config.scrollStep = v));
        y += gap;

        addRenderableWidget(new RangeSlider(x, y, w,
                config.defaultZoom, 1.0, 50.0,
                v -> String.format("%.1f", v),
                "infinizoom.config.default_zoom",
                v -> config.defaultZoom = v));
        y += gap;

        addRenderableWidget(new RangeSlider(x, y, w,
                config.maxZoomOutFov, 110.0, 179.0,
                v -> String.valueOf(Math.round(v)),
                "infinizoom.config.max_fov",
                v -> config.maxZoomOutFov = v));
        y += gap;

        addRenderableWidget(new RangeSlider(x, y, w,
                config.sensitivityMultiplier, 0.1, 3.0,
                v -> String.format("%.2f", v),
                "infinizoom.config.sensitivity_mult",
                v -> config.sensitivityMultiplier = v));
        y += gap;

        Button modeButton = Button.builder(
                modeLabel(),
                btn -> {
                    config.sensitivityMode = config.sensitivityMode.next();
                    btn.setMessage(modeLabel());
                }
        ).bounds(x, y, w, 20).build();
        addRenderableWidget(modeButton);
        y += gap;

        Button invertButton = Button.builder(
                invertLabel(),
                btn -> {
                    config.invertScroll = !config.invertScroll;
                    btn.setMessage(invertLabel());
                }
        ).bounds(x, y, w, 20).build();
        addRenderableWidget(invertButton);
        y += gap;

        Button toggleModeButton = Button.builder(
                toggleModeLabel(),
                btn -> {
                    config.toggleMode = !config.toggleMode;
                    btn.setMessage(toggleModeLabel());
                }
        ).bounds(x, y, w, 20).build();
        addRenderableWidget(toggleModeButton);
        y += gap + 6;

        addRenderableWidget(Button.builder(
                Component.translatable("infinizoom.config.reset"),
                btn -> {
                    ConfigManager.resetToDefaults();
                    this.rebuildWidgets();
                }
        ).bounds(x, y, w / 2 - 2, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("infinizoom.config.done"),
                btn -> this.onClose()
        ).bounds(x + w / 2 + 2, y, w / 2 - 2, 20).build());
    }

    private Component modeLabel() {
        return Component.translatable("infinizoom.config.sensitivity_mode", config.sensitivityMode.getDisplayName());
    }

    private Component invertLabel() {
        Component state = Component.translatable(config.invertScroll
                ? "infinizoom.toggle.on" : "infinizoom.toggle.off");
        return Component.translatable("infinizoom.config.invert_scroll", state);
    }

    private Component toggleModeLabel() {
        return Component.translatable(config.toggleMode
                ? "infinizoom.config.activation.toggle"
                : "infinizoom.config.activation.hold");
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private static class RangeSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        private final Function<Double, String> formatter;
        private final String labelKey;
        private final Consumer<Double> setter;

        RangeSlider(int x, int y, int width,
                    double current, double min, double max,
                    Function<Double, String> formatter,
                    String labelKey,
                    Consumer<Double> setter) {
            super(x, y, width, 20, Component.empty(), (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.formatter = formatter;
            this.labelKey = labelKey;
            this.setter = setter;
            updateMessage();
        }

        private double realValue() {
            return min + this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(labelKey, formatter.apply(realValue())));
        }

        @Override
        protected void applyValue() {
            setter.accept(realValue());
        }
    }
}
