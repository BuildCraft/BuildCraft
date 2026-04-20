/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib.block;

import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;

public class BlockBCBase_Neptune extends Block {
    public static final DirectionProperty PROP_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty BLOCK_FACING_6 = BlockStateProperties.FACING;

    public BlockBCBase_Neptune(BlockBehaviour.Properties prop) {
    	super(prop);
    	if (this instanceof IBlockWithFacing) {
            EnumProperty<Direction> facingProp = ((IBlockWithFacing) this).getFacingProperty();
    		this.registerDefaultState(this.stateDefinition.any().setValue(facingProp, Direction.NORTH));
        }
    }

    public BlockBCBase_Neptune() {
    	this(Properties.of(Material.METAL).strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
    }

    // BlockState

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
        if (this instanceof IBlockWithFacing) 
            bs.add(((IBlockWithFacing) this).getFacingProperty());
		super.createBlockStateDefinition(bs);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        if (this instanceof IBlockWithFacing) {
            EnumProperty<Direction> prop = ((IBlockWithFacing) this).getFacingProperty();
            Direction facing = state.getValue(prop);
            state = state.setValue(prop, rot.rotate(facing));
        }
        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
    	
        if (this instanceof IBlockWithFacing) {
            EnumProperty<Direction> prop = ((IBlockWithFacing) this).getFacingProperty();
            Direction facing = state.getValue(prop);
            state = state.setValue(prop, mirror.mirror(facing));
        }
        return state;
    }

    // Others

    @Override
	public BlockState getStateForPlacement(BlockPlaceContext bpc) {
    	LivingEntity placer = bpc.getPlayer();
    	BlockPos pos = bpc.getClickedPos();
        BlockState state = super.getStateForPlacement(bpc);
        if (this instanceof IBlockWithFacing) {
            Direction orientation = bpc.getHorizontalDirection();
            IBlockWithFacing b = (IBlockWithFacing) this;
            if (b.canFaceVertically()) {
                if (Mth.abs((float) placer.xo - pos.getX()) < 2.0F
                    && Mth.abs((float) placer.zo - pos.getZ()) < 2.0F) {
                    double y = placer.yo + placer.getEyeHeight();

                    if (y - pos.getY() > 2.0D) {
                        orientation = Direction.DOWN;
                    }

                    if (pos.getY() - y > 0.0D) {
                        orientation = Direction.UP;
                    }
                }
            }
            state = state.setValue(b.getFacingProperty(), orientation.getOpposite());
        }
        return state;
	}
    
    

	@Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation axis) {
        if(world.getBlockEntity(pos) instanceof TileBC_Neptune tile) 
        	tile.rotate(axis);
        return rotate(state, axis);
    }

/*    public static boolean isExceptBlockForAttachWithPiston(Block attachBlock) {
        return Block.isExceptBlockForAttachWithPiston(attachBlock);
    }*/
}
