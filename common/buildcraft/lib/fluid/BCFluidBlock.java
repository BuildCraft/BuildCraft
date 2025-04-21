/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;


import buildcraft.energy.BCEnergyConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BCFluidBlock extends FlowingFluidBlock {
    private boolean sticky = false;
    private final LazyValue<Boolean> displaceWater;
    private final LazyValue<Boolean> displaceLava;

    public BCFluidBlock(Supplier<? extends FlowingFluid> p_54694_, AbstractBlock.Properties properties, boolean sticky) {
        super(p_54694_, properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(LEVEL, Integer.valueOf(0))
        );
        displaceWater = new LazyValue<>(() -> this.getFluid().getAttributes().getDensity() > 1000);
        displaceLava = new LazyValue<>(() -> this.getFluid().getAttributes().getDensity() > 9000);

        this.sticky = sticky;
//        renderLayer = BlockRenderLayer.SOLID; // Calen: moved to BCEnergy#clientInit
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        Item itemInHand = context.getItemInHand().getItem();
        if (itemInHand == Items.WATER_BUCKET && displaceWater.get()) {
            return false;
        } else if (itemInHand == Items.LAVA_BUCKET && displaceLava.get()) {
            return false;
        } else {
            return true;
        }
    }

//    @Override
//    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double yToTest, Material material, boolean testingHead) {
//        if (material == Material.WATER) {
//            return true;
//        }
//        return null;
//    }

    // public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
    @Override
    public int getFlammability(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        return this.material.isFlammable() ? 200 : 0;
    }

    // public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        return this.material.isFlammable() ? 200 : 0;
    }

    // public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    @Override
    public void entityInside(BlockState p_58180_, World p_58181_, BlockPos p_58182_, Entity entityIn) {
        if (BCEnergyConfig.oilIsSticky && sticky) {
            entityIn.makeStuckInBlock(p_58180_, new Vector3d(0.25D, (double) 0.05F, 0.25D));
        }
    }

//    public void setSticky(boolean sticky) {
//        this.sticky = sticky;
//    }

    @Override
    public boolean isPathfindable(BlockState blockState, IBlockReader world, BlockPos pos, PathType type) {
        return false;
    }

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader level, BlockPos pos, @Nullable MobEntity entity) {
        return PathNodeType.LAVA;
    }
}
