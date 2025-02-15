/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;


import buildcraft.energy.BCEnergyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BCFluidBlock extends LiquidBlock {
    private boolean sticky = false;
    private final LazyLoadedValue<Boolean> displaceWater;
    private final LazyLoadedValue<Boolean> displaceLava;
    private final BCFluidRegistryContainer registryContainer;

    public BCFluidBlock(Supplier<? extends FlowingFluid> p_54694_, BlockBehaviour.Properties properties, boolean sticky, BCFluidRegistryContainer registryContainer) {
        super(p_54694_, properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(LEVEL, Integer.valueOf(0))
        );
        displaceWater = new LazyLoadedValue<>(() -> this.getFluid().getFluidType().getDensity() > 1000);
        displaceLava = new LazyLoadedValue<>(() -> this.getFluid().getFluidType().getDensity() > 9000);

        this.sticky = sticky;
        this.registryContainer = registryContainer;
//        renderLayer = BlockRenderLayer.SOLID; // Calen: moved to BCEnergy#clientInit
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
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
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.ignitedByLava() ? 200 : 0;
    }

    // public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.ignitedByLava() ? 200 : 0;
    }

    // public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    @Override
    public void entityInside(BlockState p_58180_, Level p_58181_, BlockPos p_58182_, Entity entityIn) {
        if (BCEnergyConfig.oilIsSticky && sticky) {
            entityIn.makeStuckInBlock(p_58180_, new Vec3(0.25D, (double) 0.05F, 0.25D));
        }
    }

//    public void setSticky(boolean sticky) {
//        this.sticky = sticky;
//    }

    // Calen 1.20.1
    public ResourceLocation getRegistryName() {
        return ForgeRegistries.BLOCKS.getKey(this);
    }

    @Override
    public String getDescriptionId() {
        return registryContainer.getStill().getFluidType().getDescriptionId();
    }

    @Override
    public MutableComponent getName() {
        return (MutableComponent) registryContainer.getStill().getFluidType().getDescription();
    }
}
