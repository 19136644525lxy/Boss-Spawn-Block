package com.yifei.bsb;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomBossSpawnerBlock extends Block implements BlockEntityProvider {

    public CustomBossSpawnerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomBossSpawnerBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        // 从物品栈标签加载标签和实体ID
        if (!world.isClient && itemStack.hasNbt()) {
            NbtCompound nbt = itemStack.getNbt();
            if (nbt != null && nbt.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)) {
                NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof CustomBossSpawnerBlockEntity bossSpawner) {
                    if (blockEntityTag.contains("label", NbtElement.STRING_TYPE)) {
                        bossSpawner.setLabel(blockEntityTag.getString("label"));
                    }
                    if (blockEntityTag.contains("entity_id", NbtElement.STRING_TYPE)) {
                        bossSpawner.setEntityId(blockEntityTag.getString("entity_id"));
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getPickStack(net.minecraft.world.BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        // 获取方块实体并将其数据添加到物品栈
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CustomBossSpawnerBlockEntity bossSpawner) {
            NbtCompound blockEntityTag = new NbtCompound();
            blockEntityTag.putString("label", bossSpawner.getLabel());
            blockEntityTag.putString("entity_id", bossSpawner.getEntityId());
            
            NbtCompound itemTag = new NbtCompound();
            itemTag.put("BlockEntityTag", blockEntityTag);
            stack.setNbt(itemTag);
            
            // 设置自定义名称
            stack.setCustomName(Text.literal("§6" + bossSpawner.getLabel() + " Boss Spawner"));
        }
        return stack;
    }



    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 提示玩家通过游戏菜单打开配置界面
            player.sendMessage(Text.literal("请通过游戏菜单打开配置界面：选项 -> 模组设置 -> BSB"), false);
            player.sendMessage(Text.literal("配置文件位置：.minecraft/config/bsb/"), false);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // 破坏时生成粒子效果
        if (world.isClient) {
            world.addParticle(net.minecraft.particle.ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
            world.addParticle(net.minecraft.particle.ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
        }
        super.onBreak(world, pos, state, player);
    }
}