package com.yifei.bsb;

import com.yifei.bsb.block.CustomBossSpawnerBlock;
import com.yifei.bsb.block.entity.CustomBossSpawnerBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bsb";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "bsb" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "bsb" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold BlockEntityTypes which will all be registered under the "bsb" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "bsb" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "bsb:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "bsb:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Creates a new food item with the id "bsb:honey_glazed_snacks", nutrition 1 and saturation 2
    public static final RegistryObject<Item> HONEY_GLAZED_SNACKS = ITEMS.register("honey_glazed_snacks", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Creates a custom boss spawner block
    public static final RegistryObject<Block> CUSTOM_BOSS_SPAWNER = BLOCKS.register("custom_boss_spawner", () -> new CustomBossSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()));
    // Creates a block item for the custom boss spawner
    public static final RegistryObject<Item> CUSTOM_BOSS_SPAWNER_ITEM = ITEMS.register("custom_boss_spawner", () -> new BlockItem(CUSTOM_BOSS_SPAWNER.get(), new Item.Properties()));
    // Creates a block entity type for the custom boss spawner
    public static final RegistryObject<BlockEntityType<CustomBossSpawnerBlockEntity>> CUSTOM_BOSS_SPAWNER_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("custom_boss_spawner", () -> BlockEntityType.Builder.of(CustomBossSpawnerBlockEntity::new, CUSTOM_BOSS_SPAWNER.get()).build(null));

    // Creates a creative tab with the id "bsb:example_tab" for the honey glazed snacks item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("creativetab.bsb.example_tab"))
            .icon(() -> HONEY_GLAZED_SNACKS.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(HONEY_GLAZED_SNACKS.get()); // Add the honey glazed snacks item to the tab. For your own tabs, this method is preferred over the event
                output.accept(CUSTOM_BOSS_SPAWNER_ITEM.get()); // Add the custom boss spawner to the tab
            }).build());

    public ExampleMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);



        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "bsb/common.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        // Set the static TYPE field in CustomBossSpawnerBlockEntity
        // This needs to be done after all registries are initialized
        CustomBossSpawnerBlockEntity.TYPE = CUSTOM_BOSS_SPAWNER_ENTITY_TYPE.get();
        
        // Boss spawner configuration
        LOGGER.info("BOSS SPAWN EGG >> {}", Config.bossSpawnEgg);
        LOGGER.info("BOSS SPAWN DELAY >> {}", Config.bossSpawnDelay);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
            event.accept(CUSTOM_BOSS_SPAWNER_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        // Do something when commands are registered
        LOGGER.info("HELLO from register commands");
        
        // Register custom commands
        com.yifei.bsb.command.CommandManager.registerCommands(event.getDispatcher());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            

        }
    }
}
