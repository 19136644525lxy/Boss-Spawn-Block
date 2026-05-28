package com.yifei.bsb;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CustomBossSpawnerBlockEntity extends BlockEntity {

    private boolean isActivated = false;
    private String label = "default"; // 默认标签
    private String entityId = "minecraft:ender_dragon_spawn_egg"; // 默认实体ID

    public CustomBossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(Bsb.CUSTOM_BOSS_SPAWNER_ENTITY_TYPE, pos, state);
    }

    // 设置此刷怪笼的标签
    public void setLabel(String label) {
        this.label = label;
        markDirty();
    }

    // 获取此刷怪笼的标签
    public String getLabel() {
        return label;
    }

    // 设置此刷怪笼的实体ID
    public void setEntityId(String entityId) {
        this.entityId = entityId;
        markDirty();
    }

    // 获取此刷怪笼的实体ID
    public String getEntityId() {
        return entityId;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean("isActivated", isActivated);
        nbt.putString("label", label);
        nbt.putString("entityId", entityId);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        isActivated = nbt.getBoolean("isActivated");
        if (nbt.contains("label")) {
            label = nbt.getString("label");
        }
        if (nbt.contains("entityId")) {
            entityId = nbt.getString("entityId");
        }
    }

    // 方块实体的tick方法
    public static void tick(World world, BlockPos pos, BlockState state, CustomBossSpawnerBlockEntity blockEntity) {
        if (world.isClient || blockEntity.isActivated) {
            return;
        }

        // 检查是否有生存模式的玩家在附近
        boolean hasSurvivalPlayerNearby = false;
        if (world instanceof ServerWorld serverWorld) {
            for (var player : serverWorld.getPlayers()) {
                if (player.interactionManager.getGameMode() == GameMode.SURVIVAL && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 100) {
                    hasSurvivalPlayerNearby = true;
                    break;
                }
            }
        }

        // 只有在有生存模式玩家附近时才激活刷怪笼
        if (hasSurvivalPlayerNearby) {
            blockEntity.spawnBoss(world, pos);
            blockEntity.isActivated = true;
        }
    }

    private void spawnBoss(World world, BlockPos pos) {
        // 从配置文件中读取最新的 entityId 值
        String spawnEggId = Config.getBossSpawnEggForLabel(this.label);
        
        // 验证刷怪蛋ID
        String errorMessage = validateSpawnEggId(spawnEggId);
        
        if (errorMessage != null) {
            // ID无效，向附近玩家发送错误信息
            sendErrorMessageToNearbyPlayers(world, pos, errorMessage);
            // 保留刷怪笼方块
            return;
        }
        
        // ID有效，继续生成生物
        Identifier spawnEggLocation = Identifier.tryParse(spawnEggId);
        if (spawnEggLocation == null) {
            sendErrorMessageToNearbyPlayers(world, pos, Text.translatable("message.bsb.spawn_egg.invalid.format", spawnEggId).getString());
            return;
        }
        
        ItemStack spawnEggStack = new ItemStack(Registries.ITEM.get(spawnEggLocation));

        if (spawnEggStack.getItem() instanceof SpawnEggItem spawnEgg) {
            EntityType<?> entityType = spawnEgg.getEntityType(spawnEggStack.getNbt());
            if (entityType != null) {
                Entity entity = entityType.create(world);
                if (entity != null) {
                    entity.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                    if (entity instanceof MobEntity mob && world instanceof ServerWorld serverWorld) {
                        mob.initialize(serverWorld, world.getLocalDifficulty(pos), net.minecraft.entity.SpawnReason.SPAWNER, null, null);
                    }
                    world.spawnEntity(entity);
                }
            }
        }

        // 生成boss后立即移除刷怪笼方块
        world.removeBlock(pos, false);
    }
    
    // 验证刷怪蛋ID是否有效
    private String validateSpawnEggId(String spawnEggId) {
        // 验证Identifier格式
        if (spawnEggId == null || spawnEggId.isEmpty()) {
            return Text.translatable("message.bsb.spawn_egg.invalid.empty").getString();
        }
        
        try {
            Identifier spawnEggLocation = Identifier.tryParse(spawnEggId);
            if (spawnEggLocation == null) {
                return Text.translatable("message.bsb.spawn_egg.invalid.format", spawnEggId).getString();
            }
            
            // 验证物品是否存在
            if (!Registries.ITEM.containsId(spawnEggLocation)) {
                return Text.translatable("message.bsb.spawn_egg.invalid.not_found", spawnEggId).getString();
            }
            
            // 验证物品是否为刷怪蛋
            ItemStack spawnEggStack = new ItemStack(Registries.ITEM.get(spawnEggLocation));
            if (!(spawnEggStack.getItem() instanceof SpawnEggItem)) {
                return Text.translatable("message.bsb.spawn_egg.invalid.not_egg", spawnEggId).getString();
            }
            
            // 验证是否能获取有效的EntityType
            SpawnEggItem spawnEgg = (SpawnEggItem) spawnEggStack.getItem();
            EntityType<?> entityType = spawnEgg.getEntityType(spawnEggStack.getNbt());
            if (entityType == null) {
                return Text.translatable("message.bsb.spawn_egg.invalid.no_entity", spawnEggId).getString();
            }
            
        } catch (Exception e) {
            // 格式错误
            return Text.translatable("message.bsb.spawn_egg.invalid.format", spawnEggId).getString();
        }
        
        return null; // ID有效
    }
    
    // 向附近玩家发送错误信息
    private void sendErrorMessageToNearbyPlayers(World world, BlockPos pos, String errorMessage) {
        if (world instanceof ServerWorld serverWorld) {
            for (var player : serverWorld.getPlayers()) {
                if (player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 100) {
                    player.sendMessage(Text.literal("§c" + errorMessage), false);
                    player.sendMessage(Text.translatable("message.bsb.spawn_egg.invalid.hint"), false);
                }
            }
        }
    }
}