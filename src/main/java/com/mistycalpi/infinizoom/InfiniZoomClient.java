package com.mistycalpi.infinizoom;

import com.mistycalpi.infinizoom.config.ConfigManager;
import com.mistycalpi.infinizoom.config.ZoomConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class InfiniZoomClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.MISC;

    private static KeyMapping zoomKey;
    private static KeyMapping configKey;

    private boolean wasZoomingLastTick = false;
    private boolean wasKeyDownLastTick = false;
    private boolean toggledOn = false;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.infinizoom.zoom",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                CATEGORY
        ));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.infinizoom.config",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        boolean keyDown = zoomKey.isDown();
        boolean pressed = keyDown && !wasKeyDownLastTick;
        wasKeyDownLastTick = keyDown;

        boolean active;
        if (ConfigManager.get().toggleMode) {
            if (pressed) {
                toggledOn = !toggledOn;
            }
            active = toggledOn;
        } else {
            toggledOn = false;
            active = keyDown;
        }

        if (active && !wasZoomingLastTick) {
            ZoomController.startZoom();
        }
        ZoomController.setZooming(active);
        wasZoomingLastTick = active;

        while (configKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new ZoomConfigScreen(null));
            }
        }
    }
}
