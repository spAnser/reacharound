package com.spanser.reacharound;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spanser.reacharound.client.gui.Hud;
import com.spanser.reacharound.config.ReacharoundConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class Reacharound implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static Reacharound instance;
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
        Hud hud = new Hud(client, this.config);

        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> {
            if (client.currentScreen == null) {
                hud.renderPlacementAssistText(guiGraphics, tickCounter.getTickDelta(false));
            }
        });

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
                LOGGER.warn("Could not load reacharound config: " + e.getLocalizedMessage());
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
            file.getParentFile().mkdir();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(config));
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.warn("Could not save reacharound config: " + e.getLocalizedMessage());
        }
    }
}
