package com.yifei.bsb;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bsb implements ModInitializer {
    public static final String MOD_ID = "bsb";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 自定义Boss刷怪笼方块
    public static final Block CUSTOM_BOSS_SPAWNER = new CustomBossSpawnerBlock(FabricBlockSettings.create().strength(-1.0F, 3600000.0F).nonOpaque());
    public static final BlockEntityType<CustomBossSpawnerBlockEntity> CUSTOM_BOSS_SPAWNER_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(CustomBossSpawnerBlockEntity::new, CUSTOM_BOSS_SPAWNER).build();
    public static final Item CUSTOM_BOSS_SPAWNER_ITEM = new BlockItem(CUSTOM_BOSS_SPAWNER, new FabricItemSettings());
    
    // 蜂蜜零食物品
    public static final Item HONEY_GLAZED_SNACKS = new Item(new FabricItemSettings());
    


    @Override
    public void onInitialize() {
        // 注册方块
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "custom_boss_spawner"), CUSTOM_BOSS_SPAWNER);
        // 注册方块实体类型
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "custom_boss_spawner"), CUSTOM_BOSS_SPAWNER_ENTITY_TYPE);
        // 注册物品
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "custom_boss_spawner"), CUSTOM_BOSS_SPAWNER_ITEM);
        // 注册蜂蜜零食物品
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "honey_glazed_snacks"), HONEY_GLAZED_SNACKS);

        // 添加物品到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(CUSTOM_BOSS_SPAWNER_ITEM);
        });
        
        // 添加蜂蜜零食物品到创造模式食物栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(HONEY_GLAZED_SNACKS);
        });

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandManager.registerCommands(dispatcher);
        });

        // 服务器启动事件
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("Boss Spawner Block mod initialized");
            // 加载配置
            Config.loadMappingFile();
        });
    }
}