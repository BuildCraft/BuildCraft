package ct.buildcraft.core.block;

import java.util.function.BiFunction;

import ct.buildcraft.api.enums.EnumPowerStage;
import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.core.BCCore;
import ct.buildcraft.core.blockEntity.TileEngineBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class BlockEngine extends Block implements EntityBlock{
	
	public static final Direction[] directions = new Direction[]{
		Direction.UP ,Direction.DOWN,Direction.EAST,Direction.WEST,Direction.SOUTH,Direction.NORTH
	};
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
    protected static final VoxelShape MOVING_SHAPE_NORTH = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 12.0D);
    protected static final VoxelShape TRUNK_SHAPE_NORTH = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 12.0D);
    protected static final VoxelShape BASE_ENGINE_SHAPE_NORTH = Shapes.or(TRUNK_SHAPE_NORTH, BASE_SHAPE_NORTH);
    
    
	private final BiFunction<BlockPos,BlockState,BlockEntity> newTile ;
	private final String type;
	
	public final static BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final EnumProperty<EnumPowerStage> STAGE = BuildCraftProperties.ENERGY_STAGE;
	public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
	private Boolean flag = false;

	public BlockEngine(BiFunction<BlockPos, BlockState, BlockEntity> newTile, String type) {
		super(BlockBehaviour.Properties.of(Material.METAL).strength(25.0f).explosionResistance(10.0f).dynamicShape());
		this.registerDefaultState(
				  this.stateDefinition.any().setValue(ENABLED, false).setValue(FACING, Direction.UP).setValue(STAGE, EnumPowerStage.BLUE)
				);
		this.newTile = newTile;
		this.type = type;
	}
	public boolean useShapeForLightOcclusion(BlockState p_56395_) {
		      return true;
	}

     public VoxelShape getVisualShape(BlockState p_48735_, BlockGetter p_48736_, BlockPos p_48737_, CollisionContext p_48738_) {
         return Shapes.empty();
      }
     public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
         return 1.0F;
      }

      @Override
	public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
         return true;
    }
	@Override
	public RenderShape getRenderShape(BlockState p_60550_) {
		return RenderShape.MODEL;
	}
	@Override
    public VoxelShape getCollisionShape(BlockState bs, BlockGetter lev, BlockPos pos, CollisionContext p_51179_) {
		TileEngineBase tile = (TileEngineBase)lev.getBlockEntity(pos);
		switch(bs.getValue(FACING)) {
		case UP:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_UP, MOVING_SHAPE_UP.move(0.0d, tile.progress*1/2, 0.0d)) : BASE_ENGINE_SHAPE_UP;
		case DOWN:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_DOWN, MOVING_SHAPE_DOWN.move(0.0d, -tile.progress*1/2, 0.0d)) : BASE_ENGINE_SHAPE_DOWN;
		case EAST:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_EAST, MOVING_SHAPE_EAST.move(tile.progress*1/2, 0.0d, 0.0d)) : BASE_ENGINE_SHAPE_EAST;
		case WEST:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_WEST, MOVING_SHAPE_WEST.move(-tile.progress*1/2, 0.0d, 0.0d)) : BASE_ENGINE_SHAPE_WEST;
		case SOUTH:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_SOUTH, MOVING_SHAPE_SOUTH.move(0.0d, 0.0d, tile.progress*1/2)) : BASE_ENGINE_SHAPE_SOUTH;
		case NORTH:
			return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_NORTH, MOVING_SHAPE_NORTH.move(0.0d, 0.0d, -tile.progress*1/2)) : BASE_ENGINE_SHAPE_NORTH;
		}
		return null;
//	    return tile != null ? Shapes.or(BASE_ENGINE_SHAPE_WEST, MOVING_SHAPE_WEST.move(0.0d, tile.progress*1/2, 0.0d)) : BASE_ENGINE_SHAPE_WEST;
	}
	@Override
	public VoxelShape getShape(BlockState bs, BlockGetter bg, BlockPos pos, CollisionContext cc) {
//		TileEngineBase tile = (TileEngineBase)bg.getBlockEntity(pos);
		return this.getCollisionShape(bs, bg, pos, cc);
//	    return tile != null ? Shapes.or(BASE_ENGINE_SHAPE, MOVING_SHAPE.move(0.0d, tile.progress*1/2, 0.0d)) : BASE_ENGINE_SHAPE;
    }
	

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState bs) {
	//	return BCCore.ENGINE_REDSTONE_TILE.get().create(pos, bs);

		return newTile.apply(pos,bs);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
		super.createBlockStateDefinition(bs);
		bs.add(ENABLED).add(FACING).add(CONNECTED).add(STAGE);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext bps) {
		Level lev = bps.getLevel();
		flag = lev.hasNeighborSignal(bps.getClickedPos());
		BlockPos pos = bps.getClickedPos();
		BlockEntity be ;
		boolean b = false;
		Direction d0 = Direction.UP;
		for(Direction d : directions) {
			be = lev.getBlockEntity(pos.offset(d.getNormal()));
			if(be != null&&!(be instanceof TileEngineBase)&&be.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
				d0 = d;
				b = true;
				break;
			}
		}
		return super.getStateForPlacement(bps).setValue(ENABLED, flag).setValue(FACING, d0).setValue(CONNECTED, b);
	}
	


	@Override
	public InteractionResult use(BlockState p_60503_, Level lev, BlockPos pos, Player p_60506_,
			InteractionHand p_60507_, BlockHitResult bh) {
		((TileEngineBase)lev.getBlockEntity(pos)).onActivated(p_60506_, p_60507_, bh.getDirection());
		return super.use(p_60503_, lev, pos, p_60506_, p_60507_, bh);
	}
	@Override
	public void setPlacedBy(Level lev, BlockPos pos, BlockState bs, LivingEntity p_49850_,
			ItemStack p_49851_) {
		if(bs.getValue(BlockEngine.CONNECTED)) {
			lev.getBlockEntity(pos.offset(bs.getValue(BlockEngine.FACING).getNormal()))
			.getCapability(
					ForgeCapabilities.ENERGY).ifPresent((a) -> ((TileEngineBase)lev.getBlockEntity(pos)).targe =  a );;
		}
	}
	@Override
	public void neighborChanged(BlockState bs, Level lev, BlockPos pos, Block block, BlockPos pos2, boolean p_54083_) {
		flag = lev.hasNeighborSignal(pos);
		if(flag != bs.getValue(ENABLED)) {
			lev.setBlock(pos, bs.cycle(ENABLED).setValue(ENABLED, flag), 2);
//			((TileEngineBase)lev.getBlockEntity(pos)).isRedstonePowered = flag;
//			lev.getBlockEntity();
		}
		if(!bs.getValue(CONNECTED)) {
			for(Direction d : directions) {
				if(pos.offset(d.getNormal()).equals(pos2)) {
					BlockEntity be = lev.getBlockEntity(pos2);
					if(be !=null&&be.getCapability(ForgeCapabilities.ENERGY,d.getOpposite()).isPresent()) {
						lev.setBlock(pos, bs.setValue(FACING, d).setValue(CONNECTED, true), 2);
//						lev.sendBlockUpdated(pos, bs, bs, Block.UPDATE_CLIENTS);
//						be.getCapability(
//								ForgeCapabilities.ENERGY).ifPresent((a) -> ((TileEngineBase)lev.getBlockEntity(pos)).targe =  a );
						//do something;
											
					}
				}
			}
		}else {
			if(pos.offset(bs.getValue(BlockEngine.FACING).getNormal()).equals(pos2)) {
				var be = lev.getBlockEntity(pos2);
				if(be !=null)
				be.getCapability(
						ForgeCapabilities.ENERGY).ifPresent((a) -> ((TileEngineBase)lev.getBlockEntity(pos)).targe =  a );
				else {
					lev.setBlock(pos, bs.setValue(BlockEngine.CONNECTED, false), 2);
//					((TileEngineBase)lev.getBlockEntity(pos)).targe =  null;
				}
			
			}
		}

	}
	
	
	
	
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lev, BlockState bs,
			BlockEntityType<T> bet) {
		return BCCore.ENGINE_MAP.get(type) == bet ? ($0,pos,$1,BlockEntity) -> {
			if(BlockEntity instanceof TileEngineBase) {
				((TileEngineBase) BlockEntity).tick();
			}
		} : null;
	}

	
	
	
	
	
}
