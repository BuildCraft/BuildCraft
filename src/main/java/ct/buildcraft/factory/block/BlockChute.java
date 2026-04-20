package ct.buildcraft.factory.block;

import java.util.Map;

import ct.buildcraft.api.properties.BuildCraftProperties;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

public class BlockChute extends Block{
    public static final DirectionProperty facing = BlockStateProperties.FACING;
/*    public static final BooleanProperty connected_up = BooleanProperty.create("connected_up");
    public static final BooleanProperty connected_down = BooleanProperty.create("connected_down");
    public static final BooleanProperty connected_east = BooleanProperty.create("connected_east");
    public static final BooleanProperty connected_west = BooleanProperty.create("connected_west");
    public static final BooleanProperty connected_south = BooleanProperty.create("connected_south");
    public static final BooleanProperty connected_north = BooleanProperty.create("connected_north");*/
//    public static final BooleanProperty BP = new BooleanProperty("");
	public BlockChute() {
		super(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(5.0f).explosionResistance(10.0f).noOcclusion());
		this.registerDefaultState(
				  this.stateDefinition.any()
				  .setValue(facing, Direction.DOWN)
/*				    .setValue(connected_up, false)
				    .setValue(connected_down, false)
				    .setValue(connected_east, false)
				    .setValue(connected_west, false)
				    .setValue(connected_south, false)
				    .setValue(connected_north, false)*/
				    );
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
		super.createBlockStateDefinition(bs);
		bs.add(facing);
/*		bs.add(connected_up);
		bs.add(connected_down);
		bs.add(connected_east);
		bs.add(connected_west);
		bs.add(connected_south);
		bs.add(connected_north);*/
	}
	public BlockState getStateForPlacement(BlockPlaceContext p_54041_) {
		Direction direction = p_54041_.getClickedFace();
        return this.defaultBlockState().setValue(facing, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction);
    }	

}
