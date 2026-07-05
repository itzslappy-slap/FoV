package com.mistycalpi.infinizoom.config;

import net.minecraft.network.chat.Component;

public enum SensitivityMode {
    FULL("infinizoom.sensitivity.full") {
        @Override
        public double scale(double m) {
            return 1.0;
        }
    },
    PROPORTIONAL("infinizoom.sensitivity.proportional") {
        @Override
        public double scale(double m) {
            return m;
        }
    },
    DAMPENED("infinizoom.sensitivity.dampened") {
        @Override
        public double scale(double m) {
            return Math.pow(m, 1.1);
        }
    };

    private final String translationKey;

    SensitivityMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public abstract double scale(double m);

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public SensitivityMode next() {
        SensitivityMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
