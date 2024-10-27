package com.spanser.reacharound;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.client.gui.Overlay;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spanser.reacharound.client.gui.Hud;
import com.spanser.reacharound.config.ReacharoundConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Reacharound implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static Reacharound instance;
    public KeyBinding keyBindingToggle;
    public ReacharoundConfig config;

    public static Reacharound getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Reacharound Initializing.");
        loadConfig();
        instance = this;
        MinecraftClient client = MinecraftClient.getInstance();
        Hud hud = new Hud(client, config);
        Overlay overlay = new Overlay(client, config);

        ClientTickEvents.END_CLIENT_TICK.register(PlacementFeature::tick);

        UseItemCallback.EVENT.register(PlacementFeature::useItem);

        HudRenderCallback.EVENT.register((guiGraphics, deltaTime) -> {
            if (client.currentScreen == null) {
                hud.renderPlacementAssistText(guiGraphics, deltaTime);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(overlay::render);

        keyBindingToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "reacharound.keybinding.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "reacharound.keybinding.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(PlacementFeature::keybindToggle);

        LOGGER.info("Reacharound Initialized.");
    }

    public void loadConfig() {
        File file = new File("./config/reacharound.json");
        Gson gson = new Gson();
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
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
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(config));
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.warn("Could not save reacharound config: {}", e.getLocalizedMessage());
        }
    }
}
