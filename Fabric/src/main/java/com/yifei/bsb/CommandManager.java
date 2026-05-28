package com.yifei.bsb;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class CommandManager {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bsb")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("help")
                        .executes(CommandManager::showHelp)
                )
                .then(literal("create")
                        .then(argument("label", StringArgumentType.string())
                                .executes(CommandManager::createSpawner)
                                .then(argument("args", StringArgumentType.greedyString())
                                        .executes(CommandManager::createSpawnerWithArgs)
                                )
                        )
                )
                .then(literal("list")
                        .executes(CommandManager::listSpawners)
                )
                .then(literal("give")
                        .then(argument("label", StringArgumentType.string())
                                .executes(CommandManager::giveSpawner)
                        )
                )
                .then(literal("delete")
                        .then(argument("label", StringArgumentType.string())
                                .executes(CommandManager::deleteSpawner)
                        )
                )
        );
    }

    private static int createSpawner(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        String label = StringArgumentType.getString(context, "label");
        return createSpawnerItem(player, label, "minecraft:ender_dragon_spawn_egg", null);
    }

    private static int createSpawnerWithArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        String label = StringArgumentType.getString(context, "label");
        String args = StringArgumentType.getString(context, "args");
        
        // 分割args为entityId和description
        String entityId = "";
        String description = null;
        
        // 尝试找到entityId
        String[] parts = args.split(" ");
        if (parts.length > 0) {
            // 尝试通过检查每个部分直到找到有效的部分来构建entityId
            StringBuilder entityIdBuilder = new StringBuilder();
            StringBuilder descriptionBuilder = new StringBuilder();
            boolean foundValidEntityId = false;
            
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                entityIdBuilder.append(part);
                
                // 检查当前entityIdBuilder是否是有效的实体ID或映射
                String currentEntityId = entityIdBuilder.toString();
                String convertedEntityId = Config.convertChineseToSpawnEggId(currentEntityId);
                
                // 如果是有效的实体ID（包含冒号）或映射名称
                if (currentEntityId.contains(":") || !currentEntityId.equals(convertedEntityId)) {
                    entityId = currentEntityId;
                    foundValidEntityId = true;
                    
                    // 收集剩余部分作为描述
                    for (int j = i + 1; j < parts.length; j++) {
                        if (descriptionBuilder.length() > 0) {
                            descriptionBuilder.append(" ");
                        }
                        descriptionBuilder.append(parts[j]);
                    }
                    break;
                }
                
                // 为下一部分添加空格
                entityIdBuilder.append(" ");
            }
            
            // 如果没有找到有效的实体ID，使用整个args作为entityId
            if (!foundValidEntityId) {
                entityId = args;
            } else if (descriptionBuilder.length() > 0) {
                description = descriptionBuilder.toString();
            }
        }
        
        // 转换中文名称为刷怪蛋ID（如果需要）
        String convertedEntityId = Config.convertChineseToSpawnEggId(entityId);
        
        // 如果entityId是中文名称且未提供描述，使用它作为描述
        if (!entityId.equals(convertedEntityId) && description == null) {
            description = entityId;
        }
        
        return createSpawnerItem(player, label, convertedEntityId, description);
    }

    private static int createSpawnerItem(ServerPlayerEntity player, String label, String entityId, String description) {
        // 为此标签生成配置文件
        Config.generateConfigFile(label, entityId);
        
        // 创建自定义boss刷怪笼的物品栈
        ItemStack stack = new ItemStack(Bsb.CUSTOM_BOSS_SPAWNER_ITEM);
        
        // 添加带有标签和entity_id的方块实体标签
        NbtCompound blockEntityTag = new NbtCompound();
        blockEntityTag.putString("label", label);
        blockEntityTag.putString("entity_id", entityId);
        if (description != null) {
            blockEntityTag.putString("description", description);
        }
        
        NbtCompound itemTag = new NbtCompound();
        itemTag.put("BlockEntityTag", blockEntityTag);
        stack.setNbt(itemTag);
        
        // 向物品添加自定义名称
        if (description != null) {
            stack.setCustomName(Text.literal("§6" + description));
        } else {
            stack.setCustomName(Text.literal("§6" + label + " Boss Spawner"));
        }
        
        // 向玩家 inventory 添加物品
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
        
        // 发送成功消息
        player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.create.success", label).getString()), false);
        player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.create.entity", entityId).getString()), false);
        if (description != null) {
            player.sendMessage(Text.literal("§a描述: " + description), false);
        }
        player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.create.config", label).getString()), false);
        
        return 1;
    }

    private static int listSpawners(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.list.title").getString()), false);
        
        // 从配置文件获取所有刷怪笼标签
        java.util.List<String> labels = Config.listSpawnerLabels();
        
        if (labels.isEmpty()) {
            player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.list.empty").getString()), false);
        } else {
            for (String label : labels) {
                String entityId = Config.getBossSpawnEggForLabel(label);
                player.sendMessage(Text.literal("§6" + Text.translatable("message.bsb.command.list.entry", label, entityId).getString()), false);
            }
        }
        
        return 1;
    }

    private static int giveSpawner(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        String label = StringArgumentType.getString(context, "label");
        
        // 从配置文件获取entity_id
        String entityId = Config.getBossSpawnEggForLabel(label);
        
        // 如果配置文件不存在，使用默认值
        if (entityId.equals("minecraft:ender_dragon_spawn_egg")) {
            player.sendMessage(Text.literal("§c" + Text.translatable("message.bsb.command.give.default", label).getString()), false);
        }
        
        // 创建刷怪笼物品
        return createSpawnerItem(player, label, entityId, null);
    }

    private static int deleteSpawner(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        String label = StringArgumentType.getString(context, "label");
        
        // 获取配置文件路径
        java.nio.file.Path configFile = Config.getConfigDirectory().resolve(label + ".toml");
        
        // 检查配置文件是否存在
        if (java.nio.file.Files.exists(configFile)) {
            try {
                // 删除配置文件
                java.nio.file.Files.delete(configFile);
                player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.delete.success", label).getString()), false);
                player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.delete.config", configFile.toString()).getString()), false);
            } catch (java.io.IOException e) {
                player.sendMessage(Text.literal("§c" + Text.translatable("message.bsb.command.delete.error", e.getMessage()).getString()), false);
            }
        } else {
            player.sendMessage(Text.literal("§c" + Text.translatable("message.bsb.command.delete.not_found", label).getString()), false);
        }
        
        return 1;
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        // 发送帮助消息
        player.sendMessage(Text.literal("§a" + Text.translatable("message.bsb.command.help.title").getString()), false);
        player.sendMessage(Text.literal("§6" + Text.translatable("message.bsb.command.help.create").getString()), false);
        player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.help.description").getString()), false);
        player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.help.entity_id_note").getString()), false);
        player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.help.symbol_note").getString()), false);
        player.sendMessage(Text.literal("§6" + Text.translatable("message.bsb.command.help.list").getString()), false);
        player.sendMessage(Text.literal("§6" + Text.translatable("message.bsb.command.help.give").getString()), false);
        player.sendMessage(Text.literal("§6" + Text.translatable("message.bsb.command.help.delete").getString()), false);
        player.sendMessage(Text.literal("§6" + Text.translatable("message.bsb.command.help.help").getString()), false);
        player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.help.example").getString()), false);
        player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.help.example2").getString()), false);
        player.sendMessage(Text.literal("§7" + Text.translatable("message.bsb.command.help.example3").getString()), false);
        
        return 1;
    }
}