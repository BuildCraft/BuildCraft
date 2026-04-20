/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.engine;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import ct.buildcraft.api.blocks.ICustomRotationHandler;
import ct.buildcraft.api.core.IEngineType;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.item.MultiBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockEngineBase_BC8<E extends Enum<E> & IEngineType & StringRepresentable> extends BlockBCTile_Neptune
    implements ICustomRotationHandler, EntityBlock{
    private final Map<E, BiFunction<BlockPos, BlockState, ? extends TileEngineBase_BC8>> engineTileConstructors =
        new EnumMap<>(getEngineProperty().getValueClass());
    
    protected static final VoxelShape BASE_SHAPE_UP = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape MOVING_SHAPE_UP = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 8.0D, 16.0D);//2 move
    protected static final VoxelShape TRUNK_SHAPE_UP = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_UP = Shapes.or(TRUNK_SHAPE_UP, BASE_SHAPE_UP);
    
    protected static final VoxelShape BASE_SHAPE_DOWN = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape MOVING_SHAPE_DOWN = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape TRUNK_SHAPE_DOWN = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.0D, 12.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_DOWN = Shapes.or(TRUNK_SHAPE_DOWN, BASE_SHAPE_DOWN);
    
    protected static final VoxelShape BASE_SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    protected static final VoxelShape MOVING_SHAPE_EAST = Block.box(4.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape TRUNK_SHAPE_EAST = Block.box(4.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_EAST = Shapes.or(TRUNK_SHAPE_EAST, BASE_SHAPE_EAST);
    
    protected static final VoxelShape BASE_SHAPE_WEST = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape MOVING_SHAPE_WEST = Block.box(8.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape TRUNK_SHAPE_WEST = Block.box(0.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_WEST = Shapes.or(TRUNK_SHAPE_WEST, BASE_SHAPE_WEST);
    
    protected static final VoxelShape BASE_SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    protected static final VoxelShape MOVING_SHAPE_SOUTH = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape TRUNK_SHAPE_SOUTH = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 16.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_SOUTH = Shapes.or(TRUNK_SHAPE_SOUTH, BASE_SHAPE_SOUTH);
    
    protected static final VoxelShape BASE_SHAPE_NORTH = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape MOVING_SHAPE_NORTH = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape TRUNK_SHAPE_NORTH = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 12.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_NORTH = Shapes.or(TRUNK_SHAPE_NORTH, BASE_SHAPE_NORTH);

    public BlockEngineBase_BC8(Properties material) {
        super(material);
    }
    
    public BlockEngineBase_BC8() {
    	super();
    }
    // Engine directly related methods

    public BlockEngineBase_BC8<E> registerEngine(E type, BiFunction<BlockPos, BlockState, ? extends TileEngineBase_BC8> constructor) {
            engineTileConstructors.put(type, constructor);
            return this;
    }

    public boolean isRegistered(E type) {
        return engineTileConstructors.containsKey(type);
    }



    public abstract Property<E> getEngineProperty();

    public abstract String getUnlocalizedName(E engine);

    // BlockState
    
    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
     }

     @Override
	public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
   }
     
    @Override
    public boolean useShapeForLightOcclusion(BlockState p_56395_) {
    	return false;
 	}
    
    @Override
	public boolean hasDynamicShape() {
		return true;
	}

	public VoxelShape getVisualShape(BlockState p_48735_, BlockGetter p_48736_, BlockPos p_48737_, CollisionContext p_48738_) {
        return Shapes.empty();
     }
    
	@Override
	public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter lev, BlockPos pos,
			CollisionContext p_60575_) {
		TileEngineBase_BC8 tile = (TileEngineBase_BC8)lev.getBlockEntity(pos);
		if(tile == null) 
			return BASE_ENGINE_SHAPE_UP;
		switch(tile.currentDirection) {
		case UP:
			return BASE_ENGINE_SHAPE_UP;
		case DOWN:
			return BASE_ENGINE_SHAPE_DOWN;
		case EAST:
			return BASE_ENGINE_SHAPE_EAST;
		case WEST:
			return BASE_ENGINE_SHAPE_WEST;
		case SOUTH:
			return BASE_ENGINE_SHAPE_SOUTH;
		case NORTH:
			return BASE_ENGINE_SHAPE_NORTH;
		}
		return Shapes.block();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter lev, BlockPos pos,
			CollisionContext cc) {
		TileEngineBase_BC8 tile = (TileEngineBase_BC8)lev.getBlockEntity(pos);
		if(tile == null) 
			return Shapes.empty();
		switch(tile.currentDirection) {
		case UP:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_UP, MOVING_SHAPE_UP.move(0.0d, tile.RenderProgress*1/2, 0.0d)) : BASE_ENGINE_SHAPE_UP;
		case DOWN:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_DOWN, MOVING_SHAPE_DOWN.move(0.0d, -tile.RenderProgress*1/2, 0.0d)) : BASE_ENGINE_SHAPE_DOWN;
		case EAST:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_EAST, MOVING_SHAPE_EAST.move(tile.RenderProgress*1/2, 0.0d, 0.0d)) : BASE_ENGINE_SHAPE_EAST;
		case WEST:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_WEST, MOVING_SHAPE_WEST.move(-tile.RenderProgress*1/2, 0.0d, 0.0d)) : BASE_ENGINE_SHAPE_WEST;
		case SOUTH:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_SOUTH, MOVING_SHAPE_SOUTH.move(0.0d, 0.0d, tile.RenderProgress*1/2)) : BASE_ENGINE_SHAPE_SOUTH;
		case NORTH:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_NORTH, MOVING_SHAPE_NORTH.move(0.0d, 0.0d, -tile.RenderProgress*1/2)) : BASE_ENGINE_SHAPE_NORTH;
		}
		return Shapes.block();
	}

	@Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		E engineType = state.getValue(getEngineProperty());
        BiFunction<BlockPos, BlockState, ? extends TileEngineBase_BC8> constructor = engineTileConstructors.get(engineType);
        if (constructor == null) {
            return null;
        }
        TileEngineBase_BC8 tile = constructor.apply(pos,state);
        return tile;
    }

/*    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (E engine : getEngineProperty().getAllowedValues()) {
            if (engineTileConstructors.containsKey(engine)) {
                list.add(new ItemStack(this, 1, engine.ordinal()));
            }
        }
    }*/
/*
    @Override
    public int damageDropped(BlockState state) {
        return state.getValue(getEngineProperty()).ordinal();
    }*/
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext bpc) {
		Item item = bpc.getItemInHand().getItem();
		if(item instanceof MultiBlockItem)
			return super.getStateForPlacement(bpc).setValue(getEngineProperty(), (((MultiBlockItem<E>)item).getType()));
		return super.getStateForPlacement(bpc);
	}

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, world, pos, block, fromPos, b);
        if (world.isClientSide) return;
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            return engine.attemptRotation();
        }
        return InteractionResult.FAIL;
    }
}
