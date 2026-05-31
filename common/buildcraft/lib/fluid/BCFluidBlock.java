/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraft.block.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BCFluidBlock extends BlockFluidClassic {
    private boolean sticky = false;

    public BCFluidBlock(Fluid fluid, Material material) {
        super(fluid, material);
        Boolean displaceWater = fluid.getDensity() > 1000;
        displacements.put(Blocks.WATER, displaceWater);
        displacements.put(Blocks.FLOWING_WATER, displaceWater);

        Boolean displaceLava = fluid.getDensity() > 9000;
        displacements.put(Blocks.LAVA, displaceLava);
        displacements.put(Blocks.FLOWING_LAVA, displaceLava);

        renderLayer = RenderLayer.SOLID;
    }

    @Override
    public Boolean isEntityInsideMaterial(BlockView world, BlockPos pos, BlockState state, Entity entity, double yToTest, Material material, boolean testingHead) {
        if (material == Material.WATER) {
            return true;
        }
        return null;
    }

    @Override
    public int getFlammability(BlockView world, BlockPos pos, Direction face) {
        return blockMaterial.getCanBurn() ? 200 : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockView world, BlockPos pos, Direction face) {
        return blockMaterial.getCanBurn() ? 200 : 0;
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        if (sticky) {
            entityIn.setInWeb();
        }
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }
}
