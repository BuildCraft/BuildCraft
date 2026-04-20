/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("deprecation")
public class BCFluidBlock extends LiquidBlock {
	public BCFluidBlock(FlowingFluid p_54694_, Properties p_54695_) {
		super(p_54694_, p_54695_);
		// TODO Auto-generated constructor stub
	}

	private boolean sticky = false;

    

/*	@Override
    public Boolean isEntityInsideMaterial(BlockGetter world, BlockPos pos, BlockState state, Entity entity, double yToTest, Material material, boolean testingHead) {
        if (material == Material.WATER) {
            return true;
        }
        return null;
    }*/

    @Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		
    	return this.isFlammable(state, level, pos, direction) ? 200 : 0;
	}


    @Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return this.isFlammable(state, level, pos, direction) ? 200 : 0;
	}


	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
        if (sticky) {
            entityIn.makeStuckInBlock(state, new Vec3((double)0.9F, 1.5D, (double)0.9F));//As power-snow
        }
		super.entityInside(state, level, pos, entityIn);
	}



    @Override
	public boolean isStickyBlock(BlockState state) {
		// TODO Auto-generated method stub
		return this.sticky;
	}




	public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }
}
