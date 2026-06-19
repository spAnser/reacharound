package com.spanser.reacharound;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.client.gui.Hud;
import com.spanser.reacharound.client.gui.Overlay;
import com.spanser.reacharound.config.ReacharoundConfig;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class Reacharound implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("reacharound");
    private static Reacharound instance;
    public KeyMapping keyBindingToggle;
    public ReacharoundConfig config;

    public static Reacharound getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Reacharound Initializing.");
        loadConfig();
        instance = this;

        Minecraft client = Minecraft.getInstance();
        Hud hud = new Hud(client, this.config);
        Overlay overlay = new Overlay(client, this.config);

        ClientTickEvents.END_CLIENT_TICK.register(PlacementFeature::tick);

        UseItemCallback.EVENT.register(PlacementFeature::useItem);

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath("reacharound", "placement_indicator"),
                hud::extractRenderState
        );

        LevelRenderEvents.COLLECT_SUBMITS.register(overlay::render);

        keyBindingToggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "reacharound.keybinding.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("reacharound", "keybinding/category"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(PlacementFeature::keybindToggle);

        LOGGER.info("Reacharound Initialized.");
    }

    public void loadConfig() {
        File file = new File("./config/reacharound.json");
        Gson gson = new Gson();
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
                config = gson.fromJson(fileReader, ReacharoundConfig.class);
                fileReader.close();
            } catch (IOException e) {
                LOGGER.warn("Could not load reacharound config: {}", e.getLocalizedMessage());
            }
        } else {
            config = new ReacharoundConfig();
            saveConfig();
        }
    }

    public void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File("./config/reacharound.json");
        if (!file.getParentFile().exists()) {
            if (file.getParentFile().mkdir()) {
                LOGGER.info("Created config directory.");
            } else {
                LOGGER.warn("Could not create config directory.");
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
            fileWriter.write(gson.toJson(config));
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.warn("Could not save reacharound config: {}", e.getLocalizedMessage());
        }
    }
}
