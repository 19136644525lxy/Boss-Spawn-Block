package com.yifei.bsb.block.entity;

import com.yifei.bsb.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomBossSpawnerBlockEntity extends BlockEntity {

    public static BlockEntityType<CustomBossSpawnerBlockEntity> TYPE;
    private boolean isActivated = false;
    private String label = "default"; // Default label
    private String entityId = "minecraft:ender_dragon_spawn_egg"; // Default entity ID

    public CustomBossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        if (TYPE == null) {
            throw new IllegalStateException("CustomBossSpawnerBlockEntity.TYPE has not been initialized. Make sure ExampleMod.commonSetup() has been called.");
        }
    }

    // Set label for this spawner
    public void setLabel(String label) {
        this.label = label;
        setChanged();
    }

    // Get label for this spawner
    public String getLabel() {
        return label;
    }

    // Set entity ID for this spawner
    public void setEntityId(String entityId) {
        this.entityId = entityId;
        setChanged();
    }

    // Get entity ID for this spawner
    public String getEntityId() {
        return entityId;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomBossSpawnerBlockEntity blockEntity) {
        if (level.isClientSide || blockEntity.isActivated) {
            return;
        }

        // 检查是否有生存模式的玩家在附近
        boolean hasSurvivalPlayerNearby = false;
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getPlayers(pl -> true)) {
                if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 100) {
                    hasSurvivalPlayerNearby = true;
                    break;
                }
            }
        }

        // 只有在有生存模式玩家附近时才激活刷怪笼
        if (hasSurvivalPlayerNearby) {
            blockEntity.spawnBoss(level, pos);
            blockEntity.isActivated = true;
        }
    }

    private void spawnBoss(Level level, BlockPos pos) {
        // 从配置文件中读取最新的 entityId 值
        String spawnEggId = Config.getBossSpawnEggForLabel(this.label);
        
        // 验证刷怪蛋ID
        String errorMessage = validateSpawnEggId(spawnEggId);
        
        if (errorMessage != null) {
            // ID无效，向附近玩家发送错误信息
            sendErrorMessageToNearbyPlayers(level, pos, errorMessage);
            // 保留刷怪笼方块
            return;
        }
        
        // ID有效，继续生成生物
        ResourceLocation spawnEggLocation = ResourceLocation.tryParse(spawnEggId);
        if (spawnEggLocation == null) {
            sendErrorMessageToNearbyPlayers(level, pos, Component.translatable("message.bsb.spawn_egg.invalid.format", spawnEggId).getString());
            return;
        }
        ItemStack spawnEggStack = new ItemStack(ForgeRegistries.ITEMS.getValue(spawnEggLocation));

        if (spawnEggStack.getItem() instanceof SpawnEggItem spawnEgg) {
            EntityType<?> entityType = spawnEgg.getType(spawnEggStack.getTag());
            if (entityType != null) {
                Entity entity = entityType.create(level);
                if (entity != null) {
                    entity.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                    if (entity instanceof Mob mob && level instanceof ServerLevel serverLevel) {
                        DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
                        mob.finalizeSpawn(serverLevel, difficulty, MobSpawnType.SPAWNER, null, null);
                    }
                    level.addFreshEntity(entity);
                }
            }
        }

        // 生成boss后立即移除刷怪笼方块
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
    
    // 验证刷怪蛋ID是否有效
    private String validateSpawnEggId(String spawnEggId) {
        // 验证ResourceLocation格式
        if (spawnEggId == null || spawnEggId.isEmpty()) {
            return Component.translatable("message.bsb.spawn_egg.invalid.empty").getString();
        }
        
        try {
            ResourceLocation spawnEggLocation = ResourceLocation.tryParse(spawnEggId);
            if (spawnEggLocation == null) {
                return Component.translatable("message.bsb.spawn_egg.invalid.format", spawnEggId).getString();
            }
            
            // 验证物品是否存在
            if (!ForgeRegistries.ITEMS.containsKey(spawnEggLocation)) {
                return Component.translatable("message.bsb.spawn_egg.invalid.not_found", spawnEggId).getString();
            }
            
            // 验证物品是否为刷怪蛋
            ItemStack spawnEggStack = new ItemStack(ForgeRegistries.ITEMS.getValue(spawnEggLocation));
            if (!(spawnEggStack.getItem() instanceof SpawnEggItem)) {
                return Component.translatable("message.bsb.spawn_egg.invalid.not_egg", spawnEggId).getString();
            }
            
            // 验证是否能获取有效的EntityType
            SpawnEggItem spawnEgg = (SpawnEggItem) spawnEggStack.getItem();
            EntityType<?> entityType = spawnEgg.getType(spawnEggStack.getTag());
            if (entityType == null) {
                return Component.translatable("message.bsb.spawn_egg.invalid.no_entity", spawnEggId).getString();
            }
            
        } catch (Exception e) {
            // 格式错误
            return Component.translatable("message.bsb.spawn_egg.invalid.format", spawnEggId).getString();
        }
        
        return null; // ID有效
    }
    
    // 向附近玩家发送错误信息
    private void sendErrorMessageToNearbyPlayers(Level level, BlockPos pos, String errorMessage) {
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getPlayers(pl -> true)) {
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 100) {
                    player.sendSystemMessage(Component.literal("§c" + errorMessage));
                    player.sendSystemMessage(Component.translatable("message.bsb.spawn_egg.invalid.hint"));
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isActivated", isActivated);
        tag.putString("label", label);
        tag.putString("entityId", entityId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isActivated = tag.getBoolean("isActivated");
        if (tag.contains("label")) {
            label = tag.getString("label");
        }
        if (tag.contains("entityId")) {
            entityId = tag.getString("entityId");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("isActivated", isActivated);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
