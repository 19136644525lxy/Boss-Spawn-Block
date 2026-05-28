package com.yifei.bsb.block;

import com.yifei.bsb.Config;
import com.yifei.bsb.block.entity.CustomBossSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.TooltipFlag;

public class CustomBossSpawnerBlock extends BaseEntityBlock {

    public CustomBossSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        // Get entity from block entity if exists
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        // Load label and entityId from item stack tag
        if (!level.isClientSide) {
            CustomBossSpawnerBlockEntity blockEntity = (CustomBossSpawnerBlockEntity) level.getBlockEntity(pos);
            if (blockEntity != null && stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                if (tag.contains("BlockEntityTag")) {
                    CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
                    if (blockEntityTag.contains("label")) {
                        blockEntity.setLabel(blockEntityTag.getString("label"));
                    }
                    if (blockEntityTag.contains("entity_id")) {
                        blockEntity.setEntityId(blockEntityTag.getString("entity_id"));
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        // Get block entity and add its data to item stack
        CustomBossSpawnerBlockEntity blockEntity = (CustomBossSpawnerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity != null) {
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.putString("label", blockEntity.getLabel());
            blockEntityTag.putString("entity_id", blockEntity.getEntityId());
            
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("BlockEntityTag", blockEntityTag);
            stack.setTag(itemTag);
            
            // Set custom name
            stack.setHoverName(Component.literal("§6" + blockEntity.getLabel() + " Boss Spawner"));
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // 显示原始自定义描述
        tooltip.add(Component.translatable("tooltip.bsb.custom_boss_spawner"));
        // 显示标签信息
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        String label = tag != null && tag.contains("label") ? tag.getString("label") : "default";
        
        // 从配置文件读取最新的生物ID
        String entityId = Config.getBossSpawnEggForLabel(label);
        
        // 显示标签（汉化）
        tooltip.add(Component.literal("§6标签: " + label));
        
        // 显示生物ID和中文名称
        String entityName = getTranslatedEntityName(entityId);
        tooltip.add(Component.literal("§6生成: " + entityName));
    }

    // Get translated entity name from mapping.json
    private String getTranslatedEntityName(String entityId) {
        // Use Config class to get mapped entity name
        return Config.getMappedEntityName(entityId);
    }

    @Override
    public void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        // 使用与刷怪笼相同的破坏粒子
        level.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
        level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
    }



    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public net.minecraft.client.renderer.RenderType getRenderType(BlockState state) {
        return net.minecraft.client.renderer.RenderType.cutout();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getShadeBrightness(state, level, pos);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return super.useShapeForLightOcclusion(state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // 提示玩家通过游戏菜单打开配置界面
            player.sendSystemMessage(Component.literal("请通过游戏菜单打开配置界面：选项 -> 模组设置 -> BSB"));
            player.sendSystemMessage(Component.literal("配置文件位置：.minecraft/config/bsb-client.toml"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomBossSpawnerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, CustomBossSpawnerBlockEntity.TYPE, CustomBossSpawnerBlockEntity::tick);
    }
}
