package com.mistycalpi.infinizoom.compat;

import com.mistycalpi.infinizoom.config.ZoomConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ZoomConfigScreen::new;
    }
}
