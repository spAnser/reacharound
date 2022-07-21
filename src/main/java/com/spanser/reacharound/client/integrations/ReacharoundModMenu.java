package com.spanser.reacharound.client.integrations;

import com.spanser.reacharound.client.gui.ConfigGui;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;

public class ReacharoundModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> (new ConfigGui()).getConfigScreen(parent, MinecraftClient.getInstance().world != null);
    }
}
