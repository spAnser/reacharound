package com.spanser.reacharound;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Reacharound implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static Reacharound instance;
    public ReacharoundConfig config;

    public static Reacharound getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Reacharound Initializing.");
        loadConfig();
        instance = this;
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

