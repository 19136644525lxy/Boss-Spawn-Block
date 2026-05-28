package com.yifei.bsb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class Config {
    private static java.util.Map<String, String> mappingTable = new java.util.HashMap<>();
    private static java.util.Map<String, String> reverseMappingTable = new java.util.HashMap<>();
    private static String currentLanguage = "en_us";

    // 加载映射文件
    public static void loadMappingFile() {
        Path mappingFile = getMappingDirectory().resolve("mapping.json");
        if (Files.exists(mappingFile)) {
            try {
                String content = Files.readString(mappingFile, StandardCharsets.UTF_8);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
                
                // 清空现有映射
                mappingTable.clear();
                reverseMappingTable.clear();
                
                // 加载映射
                for (String key : jsonObject.keySet()) {
                    String value = jsonObject.get(key).getAsString();
                    mappingTable.put(key, value);
                    reverseMappingTable.put(value, key);
                }
                
                Bsb.LOGGER.info("Loaded mapping file for language '{}' with {} entries", currentLanguage, mappingTable.size());
            } catch (IOException e) {
                Bsb.LOGGER.error("Failed to load mapping file for language '{}': {}", currentLanguage, e.getMessage());
                // 加载失败，保存默认映射文件
                saveMappingFile();
            }
        } else {
            // 映射文件不存在，保存默认映射文件
            saveMappingFile();
        }
    }
    
    // 保存默认映射文件
    public static void saveMappingFile() {
        Path mappingDir = getMappingDirectory();
        try {
            Files.createDirectories(mappingDir);
        } catch (IOException e) {
            Bsb.LOGGER.error("Failed to create mapping directory: {}", e.getMessage());
            return;
        }
        
        Path mappingFile = mappingDir.resolve("mapping.json");
        
        // 根据当前语言创建默认映射
        JsonObject defaultMapping = new JsonObject();
        
        if (currentLanguage.equals("zh_cn")) {
            // 中文映射
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
        } else if (currentLanguage.equals("ja_jp")) {
            // 日语映射
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
        } else if (currentLanguage.equals("ko_kr")) {
            // 韩语映射
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
        } else {
            // 英语映射（默认）
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
        }
        
        // 保存到文件
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(defaultMapping);
            Files.write(mappingFile, jsonString.getBytes(StandardCharsets.UTF_8));
            
            // 更新映射表
            mappingTable.clear();
            reverseMappingTable.clear();
            for (String key : defaultMapping.keySet()) {
                String value = defaultMapping.get(key).getAsString();
                mappingTable.put(key, value);
                reverseMappingTable.put(value, key);
            }
            
            Bsb.LOGGER.info("Generated default mapping file for language '{}' at {}", currentLanguage, mappingFile);
        } catch (IOException e) {
            Bsb.LOGGER.error("Failed to save mapping file for language '{}': {}", currentLanguage, e.getMessage());
        }
    }

    // 获取当前语言
    public static String getCurrentLanguage() {
        // 使用系统语言
        String systemLang = System.getProperty("user.language") + "_" + System.getProperty("user.country");
        if (systemLang.equals("zh_CN")) return "zh_cn";
        if (systemLang.equals("ja_JP")) return "ja_jp";
        if (systemLang.equals("ko_KR")) return "ko_kr";
        // 默认返回英语
        return "en_us";
    }

    // 获取映射目录
    public static Path getMappingDirectory() {
        currentLanguage = getCurrentLanguage();
        return FabricLoader.getInstance().getConfigDir().resolve("bsb").resolve("mapping").resolve(currentLanguage);
    }

    // 获取配置目录
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("bsb");
    }

    // 生成配置文件
    public static void generateConfigFile(String label, String entityId) {
        // 转换中文实体名称为刷怪蛋ID
        String convertedEntityId = convertChineseToSpawnEggId(entityId);
        
        Path configDir = getConfigDirectory();
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            Bsb.LOGGER.error("Failed to create config directory: {}", e.getMessage());
            return;
        }
        
        Path configFile = configDir.resolve(label + ".toml");
        if (!Files.exists(configFile)) {
            try {
                String configContent = "# Boss Spawner Configuration for " + label + "\n" +
                        "# The spawn egg to use for the boss spawner (e.g., 'minecraft:ender_dragon_spawn_egg').\n" +
                        "bossSpawnEgg = \"" + convertedEntityId + "\"\n" +
                        "# The delay (in ticks) before the boss spawner spawns the boss.\n" +
                        "bossSpawnDelay = 200\n";
                Files.write(configFile, configContent.getBytes(StandardCharsets.UTF_8));
                Bsb.LOGGER.info("Generated config file for label '{}' at {}", label, configFile);
            } catch (IOException e) {
                Bsb.LOGGER.error("Failed to generate config file for label '{}': {}", label, e.getMessage());
            }
        }
    }
    
    // 转换中文实体名称为刷怪蛋ID
    public static String convertChineseToSpawnEggId(String input) {
        // 如果输入已经是有效的刷怪蛋ID格式，直接返回
        if (input.contains(":")) {
            return input;
        }
        
        // 从映射文件返回映射的刷怪蛋ID，或如果未找到则返回原始输入
        return mappingTable.getOrDefault(input, input);
    }
    
    // 从刷怪蛋ID获取映射的实体名称
    public static String getMappedEntityName(String spawnEggId) {
        // 尝试从反向映射获取
        String mappedName = reverseMappingTable.get(spawnEggId);
        if (mappedName != null) {
            return mappedName;
        }
        
        // 如果未找到，尝试从刷怪蛋ID提取实体名称
        if (spawnEggId.contains("_spawn_egg")) {
            String entityType = spawnEggId.replace("_spawn_egg", "");
            // 移除命名空间（如果存在）
            if (entityType.contains(":")) {
                entityType = entityType.split(":")[1];
            }
            // 首字母大写并将下划线替换为空格
            entityType = entityType.substring(0, 1).toUpperCase() + entityType.substring(1).replace('_', ' ');
            return entityType;
        }
        
        return spawnEggId;
    }

    // 获取特定标签的刷怪蛋
    public static String getBossSpawnEggForLabel(String label) {
        // 尝试从配置文件加载
        Path configFile = getConfigDirectory().resolve(label + ".toml");
        if (Files.exists(configFile)) {
            try {
                String content = Files.readString(configFile);
                // 简单解析bossSpawnEgg值
                for (String line : content.split("\\n")) {
                    line = line.trim();
                    if (line.startsWith("bossSpawnEgg =")) {
                        return line.split("=\s*")[1].replaceAll("[\"']", "");
                    }
                }
            } catch (IOException e) {
                Bsb.LOGGER.error("Failed to load config file for label '{}': {}", label, e.getMessage());
            }
        }
        // 如果配置文件未找到或解析失败，返回默认值
        return "minecraft:ender_dragon_spawn_egg";
    }

    // 获取特定标签的刷怪延迟
    public static int getBossSpawnDelayForLabel(String label) {
        // 尝试从配置文件加载
        Path configFile = getConfigDirectory().resolve(label + ".toml");
        if (Files.exists(configFile)) {
            try {
                String content = Files.readString(configFile);
                // 简单解析bossSpawnDelay值
                for (String line : content.split("\\n")) {
                    line = line.trim();
                    if (line.startsWith("bossSpawnDelay =")) {
                        return Integer.parseInt(line.split("=\s*")[1]);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                Bsb.LOGGER.error("Failed to load config file for label '{}': {}", label, e.getMessage());
            }
        }
        // 如果配置文件未找到或解析失败，返回默认值
        return 200;
    }

    // 列出所有刷怪笼标签
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
                Bsb.LOGGER.error("Failed to list config files: {}", e.getMessage());
            }
        }
        return labels;
    }
}