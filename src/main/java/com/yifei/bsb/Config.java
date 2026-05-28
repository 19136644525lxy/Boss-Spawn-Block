package com.yifei.bsb;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

// Config class for Boss Spawner Block
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Boss spawner configuration
    private static final ForgeConfigSpec.ConfigValue<String> BOSS_SPAWN_EGG = BUILDER
            .comment("The spawn egg to use for the boss spawner (e.g., 'minecraft:ender_dragon_spawn_egg').")
            .define("bossSpawnEgg", "minecraft:ender_dragon_spawn_egg");

    private static final ForgeConfigSpec.IntValue BOSS_SPAWN_DELAY = BUILDER
            .comment("The delay (in ticks) before the boss spawner spawns the boss.")
            .defineInRange("bossSpawnDelay", 200, 0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String bossSpawnEgg;
    public static int bossSpawnDelay;
    private static java.util.Map<String, String> mappingTable = new java.util.HashMap<>();
    private static java.util.Map<String, String> reverseMappingTable = new java.util.HashMap<>();
    private static String currentLanguage = "en_us";

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        // load boss spawner configuration
        bossSpawnEgg = BOSS_SPAWN_EGG.get();
        bossSpawnDelay = BOSS_SPAWN_DELAY.get();
        // load mapping file
        loadMappingFile();
    }
    
    // Load mapping file from config directory
    public static void loadMappingFile() {
        Path mappingFile = getMappingDirectory().resolve("mapping.json");
        if (Files.exists(mappingFile)) {
            try {
                String content = Files.readString(mappingFile, StandardCharsets.UTF_8);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
                
                // Clear existing mappings
                mappingTable.clear();
                reverseMappingTable.clear();
                
                // Load mappings
                for (String key : jsonObject.keySet()) {
                    String value = jsonObject.get(key).getAsString();
                    mappingTable.put(key, value);
                    reverseMappingTable.put(value, key);
                }
                
                ExampleMod.LOGGER.info("Loaded mapping file for language '{}' with {} entries", currentLanguage, mappingTable.size());
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Failed to load mapping file for language '{}': {}", currentLanguage, e.getMessage());
                // If loading fails, save default mapping file
                saveMappingFile();
            }
        } else {
            // If mapping file doesn't exist, save default mapping file
            saveMappingFile();
        }
    }
    
    // Save default mapping file to config directory
    public static void saveMappingFile() {
        Path mappingDir = getMappingDirectory();
        try {
            Files.createDirectories(mappingDir);
        } catch (IOException e) {
            ExampleMod.LOGGER.error("Failed to create mapping directory: {}", e.getMessage());
            return;
        }
        
        Path mappingFile = mappingDir.resolve("mapping.json");
        
        // Create default mapping based on current language
        JsonObject defaultMapping = new JsonObject();
        
        if (currentLanguage.equals("zh_cn")) {
            // Chinese mapping
            defaultMapping.addProperty("牛", "minecraft:cow_spawn_egg");
            defaultMapping.addProperty("猪", "minecraft:pig_spawn_egg");
            defaultMapping.addProperty("羊", "minecraft:sheep_spawn_egg");
            defaultMapping.addProperty("鸡", "minecraft:chicken_spawn_egg");
            defaultMapping.addProperty("末影龙", "minecraft:ender_dragon_spawn_egg");
            defaultMapping.addProperty("凋灵", "minecraft:wither_spawn_egg");
            defaultMapping.addProperty("僵尸", "minecraft:zombie_spawn_egg");
            defaultMapping.addProperty("骷髅", "minecraft:skeleton_spawn_egg");
            defaultMapping.addProperty("蜘蛛", "minecraft:spider_spawn_egg");
            defaultMapping.addProperty("苦力怕", "minecraft:creeper_spawn_egg");
            defaultMapping.addProperty("狐狸", "minecraft:fox_spawn_egg");
            defaultMapping.addProperty("熊猫", "minecraft:panda_spawn_egg");
            defaultMapping.addProperty("北极熊", "minecraft:polar_bear_spawn_egg");
            defaultMapping.addProperty("海龟", "minecraft:turtle_spawn_egg");
            defaultMapping.addProperty("美西螈", "minecraft:axolotl_spawn_egg");
            defaultMapping.addProperty("青蛙", "minecraft:frog_spawn_egg");
            defaultMapping.addProperty("山羊", "minecraft:goat_spawn_egg");
            defaultMapping.addProperty("蝙蝠", "minecraft:bat_spawn_egg");
            defaultMapping.addProperty("豹猫", "minecraft:ocelot_spawn_egg");
            defaultMapping.addProperty("猫", "minecraft:cat_spawn_egg");
            defaultMapping.addProperty("狼", "minecraft:wolf_spawn_egg");
            defaultMapping.addProperty("马", "minecraft:horse_spawn_egg");
            defaultMapping.addProperty("驴", "minecraft:donkey_spawn_egg");
            defaultMapping.addProperty("骡", "minecraft:mule_spawn_egg");
            defaultMapping.addProperty("骷髅马", "minecraft:skeleton_horse_spawn_egg");
            defaultMapping.addProperty("僵尸马", "minecraft:zombie_horse_spawn_egg");
            defaultMapping.addProperty("骆驼", "minecraft:camel_spawn_egg");
            defaultMapping.addProperty("羊驼", "minecraft:llama_spawn_egg");
            defaultMapping.addProperty("商人羊驼", "minecraft:trader_llama_spawn_egg");
        } else if (currentLanguage.equals("ja_jp")) {
            // Japanese mapping
            defaultMapping.addProperty("牛", "minecraft:cow_spawn_egg");
            defaultMapping.addProperty("豚", "minecraft:pig_spawn_egg");
            defaultMapping.addProperty("羊", "minecraft:sheep_spawn_egg");
            defaultMapping.addProperty("鶏", "minecraft:chicken_spawn_egg");
            defaultMapping.addProperty("エンダードラゴン", "minecraft:ender_dragon_spawn_egg");
            defaultMapping.addProperty("ウィザー", "minecraft:wither_spawn_egg");
            defaultMapping.addProperty("ゾンビ", "minecraft:zombie_spawn_egg");
            defaultMapping.addProperty("スケルトン", "minecraft:skeleton_spawn_egg");
            defaultMapping.addProperty("クモ", "minecraft:spider_spawn_egg");
            defaultMapping.addProperty("クリーパー", "minecraft:creeper_spawn_egg");
            defaultMapping.addProperty("キツネ", "minecraft:fox_spawn_egg");
            defaultMapping.addProperty("パンダ", "minecraft:panda_spawn_egg");
            defaultMapping.addProperty("ホッキョクグマ", "minecraft:polar_bear_spawn_egg");
            defaultMapping.addProperty("カメ", "minecraft:turtle_spawn_egg");
            defaultMapping.addProperty("アホロートル", "minecraft:axolotl_spawn_egg");
            defaultMapping.addProperty("カエル", "minecraft:frog_spawn_egg");
            defaultMapping.addProperty("ヤギ", "minecraft:goat_spawn_egg");
            defaultMapping.addProperty("コウモリ", "minecraft:bat_spawn_egg");
            defaultMapping.addProperty("オセロット", "minecraft:ocelot_spawn_egg");
            defaultMapping.addProperty("ネコ", "minecraft:cat_spawn_egg");
            defaultMapping.addProperty("オオカミ", "minecraft:wolf_spawn_egg");
            defaultMapping.addProperty("ウマ", "minecraft:horse_spawn_egg");
            defaultMapping.addProperty("ロバ", "minecraft:donkey_spawn_egg");
            defaultMapping.addProperty("ラバ", "minecraft:mule_spawn_egg");
            defaultMapping.addProperty("スケルトンホース", "minecraft:skeleton_horse_spawn_egg");
            defaultMapping.addProperty("ゾンビホース", "minecraft:zombie_horse_spawn_egg");
            defaultMapping.addProperty("ラクダ", "minecraft:camel_spawn_egg");
            defaultMapping.addProperty("リャマ", "minecraft:llama_spawn_egg");
            defaultMapping.addProperty("トレーダーリャマ", "minecraft:trader_llama_spawn_egg");
        } else if (currentLanguage.equals("ko_kr")) {
            // Korean mapping
            defaultMapping.addProperty("소", "minecraft:cow_spawn_egg");
            defaultMapping.addProperty("돼지", "minecraft:pig_spawn_egg");
            defaultMapping.addProperty("양", "minecraft:sheep_spawn_egg");
            defaultMapping.addProperty("닭", "minecraft:chicken_spawn_egg");
            defaultMapping.addProperty("엔더 드래곤", "minecraft:ender_dragon_spawn_egg");
            defaultMapping.addProperty("위더", "minecraft:wither_spawn_egg");
            defaultMapping.addProperty("좀비", "minecraft:zombie_spawn_egg");
            defaultMapping.addProperty("스켈레톤", "minecraft:skeleton_spawn_egg");
            defaultMapping.addProperty("거미", "minecraft:spider_spawn_egg");
            defaultMapping.addProperty("크리퍼", "minecraft:creeper_spawn_egg");
            defaultMapping.addProperty("여우", "minecraft:fox_spawn_egg");
            defaultMapping.addProperty("팬더", "minecraft:panda_spawn_egg");
            defaultMapping.addProperty("북극곰", "minecraft:polar_bear_spawn_egg");
            defaultMapping.addProperty("거북이", "minecraft:turtle_spawn_egg");
            defaultMapping.addProperty("악сolotl", "minecraft:axolotl_spawn_egg");
            defaultMapping.addProperty("개구리", "minecraft:frog_spawn_egg");
            defaultMapping.addProperty("염소", "minecraft:goat_spawn_egg");
            defaultMapping.addProperty("박쥐", "minecraft:bat_spawn_egg");
            defaultMapping.addProperty("오셀로트", "minecraft:ocelot_spawn_egg");
            defaultMapping.addProperty("고양이", "minecraft:cat_spawn_egg");
            defaultMapping.addProperty("늑대", "minecraft:wolf_spawn_egg");
            defaultMapping.addProperty("말", "minecraft:horse_spawn_egg");
            defaultMapping.addProperty("당나귀", "minecraft:donkey_spawn_egg");
            defaultMapping.addProperty("노새", "minecraft:mule_spawn_egg");
            defaultMapping.addProperty("스켈레톤 말", "minecraft:skeleton_horse_spawn_egg");
            defaultMapping.addProperty("좀비 말", "minecraft:zombie_horse_spawn_egg");
            defaultMapping.addProperty("낙타", "minecraft:camel_spawn_egg");
            defaultMapping.addProperty("라마", "minecraft:llama_spawn_egg");
            defaultMapping.addProperty("상인 라마", "minecraft:trader_llama_spawn_egg");
        } else {
            // English mapping (default)
            defaultMapping.addProperty("Cow", "minecraft:cow_spawn_egg");
            defaultMapping.addProperty("Pig", "minecraft:pig_spawn_egg");
            defaultMapping.addProperty("Sheep", "minecraft:sheep_spawn_egg");
            defaultMapping.addProperty("Chicken", "minecraft:chicken_spawn_egg");
            defaultMapping.addProperty("Ender Dragon", "minecraft:ender_dragon_spawn_egg");
            defaultMapping.addProperty("Wither", "minecraft:wither_spawn_egg");
            defaultMapping.addProperty("Zombie", "minecraft:zombie_spawn_egg");
            defaultMapping.addProperty("Skeleton", "minecraft:skeleton_spawn_egg");
            defaultMapping.addProperty("Spider", "minecraft:spider_spawn_egg");
            defaultMapping.addProperty("Creeper", "minecraft:creeper_spawn_egg");
            defaultMapping.addProperty("Fox", "minecraft:fox_spawn_egg");
            defaultMapping.addProperty("Panda", "minecraft:panda_spawn_egg");
            defaultMapping.addProperty("Polar Bear", "minecraft:polar_bear_spawn_egg");
            defaultMapping.addProperty("Turtle", "minecraft:turtle_spawn_egg");
            defaultMapping.addProperty("Axolotl", "minecraft:axolotl_spawn_egg");
            defaultMapping.addProperty("Frog", "minecraft:frog_spawn_egg");
            defaultMapping.addProperty("Goat", "minecraft:goat_spawn_egg");
            defaultMapping.addProperty("Bat", "minecraft:bat_spawn_egg");
            defaultMapping.addProperty("Ocelot", "minecraft:ocelot_spawn_egg");
            defaultMapping.addProperty("Cat", "minecraft:cat_spawn_egg");
            defaultMapping.addProperty("Wolf", "minecraft:wolf_spawn_egg");
            defaultMapping.addProperty("Horse", "minecraft:horse_spawn_egg");
            defaultMapping.addProperty("Donkey", "minecraft:donkey_spawn_egg");
            defaultMapping.addProperty("Mule", "minecraft:mule_spawn_egg");
            defaultMapping.addProperty("Skeleton Horse", "minecraft:skeleton_horse_spawn_egg");
            defaultMapping.addProperty("Zombie Horse", "minecraft:zombie_horse_spawn_egg");
            defaultMapping.addProperty("Camel", "minecraft:camel_spawn_egg");
            defaultMapping.addProperty("Llama", "minecraft:llama_spawn_egg");
            defaultMapping.addProperty("Trader Llama", "minecraft:trader_llama_spawn_egg");
        }
        
        // Save to file
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(defaultMapping);
            Files.write(mappingFile, jsonString.getBytes(StandardCharsets.UTF_8));
            
            // Update mapping tables
            mappingTable.clear();
            reverseMappingTable.clear();
            for (String key : defaultMapping.keySet()) {
                String value = defaultMapping.get(key).getAsString();
                mappingTable.put(key, value);
                reverseMappingTable.put(value, key);
            }
            
            ExampleMod.LOGGER.info("Generated default mapping file for language '{}' at {}", currentLanguage, mappingFile);
        } catch (IOException e) {
            ExampleMod.LOGGER.error("Failed to save mapping file for language '{}': {}", currentLanguage, e.getMessage());
        }
    }

    // Get current language
    public static String getCurrentLanguage() {
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            if (Minecraft.getInstance().getLanguageManager() != null) {
                String languageCode = Minecraft.getInstance().getLanguageManager().getSelected().toString();
                return languageCode != null && !languageCode.isEmpty() ? languageCode : "en_us";
            }
            return "en_us";
        }
        return "en_us";
    }

    // Get mapping directory for current language
    public static Path getMappingDirectory() {
        currentLanguage = getCurrentLanguage();
        return getConfigDirectory().resolve("mapping").resolve(currentLanguage);
    }

    // Get config directory path
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve("bsb");
    }

    // Create config directory if it doesn't exist
    public static void createConfigDirectory() {
        Path configDir = getConfigDirectory();
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Failed to create config directory: {}", e.getMessage());
            }
        }
    }

    // Generate config file for a specific label
    public static void generateConfigFile(String label, String entityId) {
        // Convert Chinese entity names to spawn egg IDs
        String convertedEntityId = convertChineseToSpawnEggId(entityId);
        
        createConfigDirectory();
        Path configFile = getConfigDirectory().resolve(label + ".toml");
        if (!Files.exists(configFile)) {
            try {
                String configContent = "# Boss Spawner Configuration for " + label + "\n" +
                        "# The spawn egg to use for the boss spawner (e.g., 'minecraft:ender_dragon_spawn_egg').\n" +
                        "bossSpawnEgg = \"" + convertedEntityId + "\"\n" +
                        "# The delay (in ticks) before the boss spawner spawns the boss.\n" +
                        "bossSpawnDelay = 200\n";
                Files.write(configFile, configContent.getBytes(StandardCharsets.UTF_8));
                ExampleMod.LOGGER.info("Generated config file for label '{}' at {}", label, configFile);
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Failed to generate config file for label '{}': {}", label, e.getMessage());
            }
        }
    }
    
    // Convert Chinese entity names to spawn egg IDs
    public static String convertChineseToSpawnEggId(String input) {
        // If input is already a valid spawn egg ID format, return it directly
        if (input.contains(":")) {
            return input;
        }
        
        // Return mapped spawn egg ID from mapping file or original input if not found
        return mappingTable.getOrDefault(input, input);
    }
    
    // Get mapped entity name from spawn egg ID
    public static String getMappedEntityName(String spawnEggId) {
        // Try to get from reverse mapping
        String mappedName = reverseMappingTable.get(spawnEggId);
        if (mappedName != null) {
            return mappedName;
        }
        
        // If not found, try to extract entity name from spawn egg ID
        if (spawnEggId.contains("_spawn_egg")) {
            String entityType = spawnEggId.replace("_spawn_egg", "");
            // Remove namespace if present
            if (entityType.contains(":")) {
                entityType = entityType.split(":")[1];
            }
            // Capitalize first letter and replace underscores with spaces
            entityType = entityType.substring(0, 1).toUpperCase() + entityType.substring(1).replace('_', ' ');
            return entityType;
        }
        
        return spawnEggId;
    }

    // Get spawn egg for a specific label
    public static String getBossSpawnEggForLabel(String label) {
        // Try to load from config file
        Path configFile = getConfigDirectory().resolve(label + ".toml");
        if (Files.exists(configFile)) {
            try {
                String content = Files.readString(configFile);
                // Simple parsing for bossSpawnEgg value
                for (String line : content.split("\\n")) {
                    line = line.trim();
                    if (line.startsWith("bossSpawnEgg =")) {
                        return line.split("=\s*")[1].replaceAll("[\"']", "");
                    }
                }
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Failed to load config file for label '{}': {}", label, e.getMessage());
            }
        }
        // Return default value if config file not found or parsing failed
        return "minecraft:ender_dragon_spawn_egg";
    }

    // Get spawn delay for a specific label
    public static int getBossSpawnDelayForLabel(String label) {
        // Try to load from config file
        Path configFile = getConfigDirectory().resolve(label + ".toml");
        if (Files.exists(configFile)) {
            try {
                String content = Files.readString(configFile);
                // Simple parsing for bossSpawnDelay value
                for (String line : content.split("\\n")) {
                    line = line.trim();
                    if (line.startsWith("bossSpawnDelay =")) {
                        return Integer.parseInt(line.split("=\s*")[1]);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                ExampleMod.LOGGER.error("Failed to load config file for label '{}': {}", label, e.getMessage());
            }
        }
        // Return default value if config file not found or parsing failed
        return 200;
    }

    // List all spawner labels from config files
    public static java.util.List<String> listSpawnerLabels() {
        java.util.List<String> labels = new java.util.ArrayList<>();
        Path configDir = getConfigDirectory();
        if (Files.exists(configDir)) {
            try {
                Files.newDirectoryStream(configDir, "*.toml").forEach(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.endsWith(".toml")) {
                        labels.add(fileName.substring(0, fileName.length() - 5));
                    }
                });
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Failed to list config files: {}", e.getMessage());
            }
        }
        return labels;
    }
}
