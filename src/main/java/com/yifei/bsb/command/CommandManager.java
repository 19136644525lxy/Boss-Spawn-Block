package com.yifei.bsb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yifei.bsb.ExampleMod;
import com.yifei.bsb.block.entity.CustomBossSpawnerBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CommandManager {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bsb")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("help")
                        .executes(CommandManager::showHelp)
                )
                .then(Commands.literal("create")
                        .then(Commands.argument("label", StringArgumentType.string())
                                .executes(CommandManager::createSpawner)
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(CommandManager::createSpawnerWithArgs)
                                )
                        )
                )
                .then(Commands.literal("list")
                        .executes(CommandManager::listSpawners)
                )
                .then(Commands.literal("give")
                        .then(Commands.argument("label", StringArgumentType.string())
                                .executes(CommandManager::giveSpawner)
                        )
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument("label", StringArgumentType.string())
                                .executes(CommandManager::deleteSpawner)
                        )
                )
        );
    }

    private static int createSpawner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String label = StringArgumentType.getString(context, "label");
        return createSpawnerItem(player, label, "minecraft:ender_dragon_spawn_egg", null);
    }

    private static int createSpawnerWithArgs(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String label = StringArgumentType.getString(context, "label");
        String args = StringArgumentType.getString(context, "args");
        
        // Split args into entityId and description
        String entityId = "";
        String description = null;
        
        // Try to find the entityId
        String[] parts = args.split(" ");
        if (parts.length > 0) {
            // Try to build entityId by checking each part until we find a valid one
            StringBuilder entityIdBuilder = new StringBuilder();
            StringBuilder descriptionBuilder = new StringBuilder();
            boolean foundValidEntityId = false;
            
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                entityIdBuilder.append(part);
                
                // Check if current entityIdBuilder is a valid entity ID or mapping
                String currentEntityId = entityIdBuilder.toString();
                String convertedEntityId = com.yifei.bsb.Config.convertChineseToSpawnEggId(currentEntityId);
                
                // If it's a valid entity ID (contains colon) or a mapped name
                if (currentEntityId.contains(":") || !currentEntityId.equals(convertedEntityId)) {
                    entityId = currentEntityId;
                    foundValidEntityId = true;
                    
                    // Collect remaining parts as description
                    for (int j = i + 1; j < parts.length; j++) {
                        if (descriptionBuilder.length() > 0) {
                            descriptionBuilder.append(" ");
                        }
                        descriptionBuilder.append(parts[j]);
                    }
                    break;
                }
                
                // Add space for next part
                entityIdBuilder.append(" ");
            }
            
            // If no valid entity ID found, use the whole args as entityId
            if (!foundValidEntityId) {
                entityId = args;
            } else if (descriptionBuilder.length() > 0) {
                description = descriptionBuilder.toString();
            }
        }
        
        // Convert Chinese name to spawn egg ID if needed
        String convertedEntityId = com.yifei.bsb.Config.convertChineseToSpawnEggId(entityId);
        
        // If entityId was a Chinese name and no description provided, use it as description
        if (!entityId.equals(convertedEntityId) && description == null) {
            description = entityId;
        }
        
        return createSpawnerItem(player, label, convertedEntityId, description);
    }

    private static int createSpawnerItem(ServerPlayer player, String label, String entityId, String description) {
        // Generate config file for this label
        com.yifei.bsb.Config.generateConfigFile(label, entityId);
        
        // Create item stack for custom boss spawner
        ItemStack stack = new ItemStack(ExampleMod.CUSTOM_BOSS_SPAWNER_ITEM.get());
        
        // Add block entity tag with label and entity_id
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("label", label);
        blockEntityTag.putString("entity_id", entityId);
        if (description != null) {
            blockEntityTag.putString("description", description);
        }
        
        CompoundTag itemTag = new CompoundTag();
        itemTag.put("BlockEntityTag", blockEntityTag);
        stack.setTag(itemTag);
        
        // Add custom name to item
        if (description != null) {
            stack.setHoverName(Component.literal("§6" + description));
        } else {
            stack.setHoverName(Component.literal("§6" + label + " Boss Spawner"));
        }
        
        // Add item to player inventory
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        
        // Send success message
        player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.create.success", label).getString()));
        player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.create.entity", entityId).getString()));
        if (description != null) {
            player.sendSystemMessage(Component.literal("§a描述: " + description));
        }
        player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.create.config", label).getString()));
        
        return 1;
    }

    private static int listSpawners(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.list.title").getString()));
        
        // Get all spawner labels from config files
        java.util.List<String> labels = com.yifei.bsb.Config.listSpawnerLabels();
        
        if (labels.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.list.empty").getString()));
        } else {
            for (String label : labels) {
                String entityId = com.yifei.bsb.Config.getBossSpawnEggForLabel(label);
                player.sendSystemMessage(Component.literal("§6" + Component.translatable("message.bsb.command.list.entry", label, entityId).getString()));
            }
        }
        
        return 1;
    }

    private static int giveSpawner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String label = StringArgumentType.getString(context, "label");
        
        // Get entity_id from config file
        String entityId = com.yifei.bsb.Config.getBossSpawnEggForLabel(label);
        
        // If config file doesn't exist, use default
        if (entityId.equals("minecraft:ender_dragon_spawn_egg")) {
            player.sendSystemMessage(Component.literal("§c" + Component.translatable("message.bsb.command.give.default", label).getString()));
        }
        
        // Create spawner item
        return createSpawnerItem(player, label, entityId, null);
    }

    private static int deleteSpawner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String label = StringArgumentType.getString(context, "label");
        
        // Get config file path
        java.nio.file.Path configFile = com.yifei.bsb.Config.getConfigDirectory().resolve(label + ".toml");
        
        // Check if config file exists
        if (java.nio.file.Files.exists(configFile)) {
            try {
                // Delete config file
                java.nio.file.Files.delete(configFile);
                player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.delete.success", label).getString()));
                player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.delete.config", configFile.toString()).getString()));
            } catch (java.io.IOException e) {
                player.sendSystemMessage(Component.literal("§c" + Component.translatable("message.bsb.command.delete.error", e.getMessage()).getString()));
            }
        } else {
            player.sendSystemMessage(Component.literal("§c" + Component.translatable("message.bsb.command.delete.not_found", label).getString()));
        }
        
        return 1;
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        
        // Send help messages
        player.sendSystemMessage(Component.literal("§a" + Component.translatable("message.bsb.command.help.title").getString()));
        player.sendSystemMessage(Component.literal("§6" + Component.translatable("message.bsb.command.help.create").getString()));
        player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.help.description").getString()));
        player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.help.entity_id_note").getString()));
        player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.help.symbol_note").getString()));
        player.sendSystemMessage(Component.literal("§6" + Component.translatable("message.bsb.command.help.list").getString()));
        player.sendSystemMessage(Component.literal("§6" + Component.translatable("message.bsb.command.help.give").getString()));
        player.sendSystemMessage(Component.literal("§6" + Component.translatable("message.bsb.command.help.delete").getString()));
        player.sendSystemMessage(Component.literal("§6" + Component.translatable("message.bsb.command.help.help").getString()));
        player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.help.example").getString()));
        player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.help.example2").getString()));
        player.sendSystemMessage(Component.literal("§7" + Component.translatable("message.bsb.command.help.example3").getString()));
        
        return 1;
    }
}
