package com.yifei.bsb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yifei.bsb.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("bsb").resolve("config.json");
    
    private static ConfigData configData;
    
    public static void initialize() {
        // Load default config from resources
        try (InputStream inputStream = ConfigManager.class.getResourceAsStream("/default_config.json")) {
            if (inputStream != null) {
                configData = GSON.fromJson(new String(inputStream.readAllBytes()), ConfigData.class);
            } else {
                ExampleMod.LOGGER.error("Could not find default_config.json in resources");
                configData = new ConfigData();
            }
        } catch (IOException e) {
            ExampleMod.LOGGER.error("Error loading default config: {}", e.getMessage());
            configData = new ConfigData();
        }
        
        // Load user config from config directory
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                ConfigData userConfig = GSON.fromJson(new String(inputStream.readAllBytes()), ConfigData.class);
                if (userConfig != null) {
                    // Merge user config with default config
                    if (userConfig.bossSpawner != null) {
                        if (userConfig.bossSpawner.bossSpawnEgg != null) {
                            configData.bossSpawner.bossSpawnEgg = userConfig.bossSpawner.bossSpawnEgg;
                        }
                        configData.bossSpawner.bossSpawnDelay = userConfig.bossSpawner.bossSpawnDelay;
                    }
                }
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Error loading user config: {}", e.getMessage());
            }
        } else {
            // Save default config to config directory
            saveConfig();
        }
        
        ExampleMod.LOGGER.info("Config initialized: bossSpawnEgg={}, bossSpawnDelay={}", 
                configData.bossSpawner.bossSpawnEgg, configData.bossSpawner.bossSpawnDelay);
    }
    
    public static void saveConfig() {
        try {
            // Create directory if it doesn't exist
            Path parentDir = CONFIG_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Save config to file
            try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
                outputStream.write(GSON.toJson(configData).getBytes());
            }
            
            ExampleMod.LOGGER.info("Config saved to: {}", CONFIG_PATH);
        } catch (IOException e) {
            ExampleMod.LOGGER.error("Error saving config: {}", e.getMessage());
        }
    }
    
    public static String getBossSpawnEgg() {
        return configData.bossSpawner.bossSpawnEgg;
    }
    
    public static int getBossSpawnDelay() {
        return configData.bossSpawner.bossSpawnDelay;
    }
    
    public static void setBossSpawnEgg(String bossSpawnEgg) {
        configData.bossSpawner.bossSpawnEgg = bossSpawnEgg;
        saveConfig();
    }
    
    public static void setBossSpawnDelay(int bossSpawnDelay) {
        configData.bossSpawner.bossSpawnDelay = bossSpawnDelay;
        saveConfig();
    }
    
    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
    
    private static class ConfigData {
        public BossSpawnerConfig bossSpawner = new BossSpawnerConfig();
        
        private static class BossSpawnerConfig {
            public String bossSpawnEgg = "minecraft:ender_dragon_spawn_egg";
            public int bossSpawnDelay = 0;
        }
    }
}