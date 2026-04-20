package ct.buildcraft.factory.block;

import java.util.HashMap;
import java.util.Map;

import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.api.tools.IToolWrench;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.tile.EntityBlockFloodGate;
import ct.buildcraft.factory.tile.TileFloodGate;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockFloodGate extends BlockBCTile_Neptune{
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(Direction.UP);
    }
    
	public BlockFloodGate() {
//		super(BlockBehaviour.Properties.of(Material.METAL).strength(25.0f).explosionResistance(10.0f));
		BlockState definetion = this.stateDefinition.any();
//		CONNECTED_MAP.values().forEach((a) -> definetion.setValue(a, true));
//		this.registerDefaultState(definetion);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
//		CONNECTED_MAP.values().forEach(bs::add);
		super.createBlockStateDefinition(bs);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return BCFactoryBlocks.ENTITYBLOCKFLOODGATE.get().create(p_153215_, p_153216_);
	}
	
/*	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFloodGate) {
            for (Direction side : CONNECTED_MAP.keySet()) {
                state = state.setValue(CONNECTED_MAP.get(side), ((TileFloodGate) tile).openSides.contains(side));
            }
        }
		super.setPlacedBy(world, pos, state, placer, stack);
	}*/

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof IToolWrench) {
            if (!world.isClientSide) {
            	Direction side = hit.getDirection();
                if (side != Direction.UP) {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof TileFloodGate) {
                        if (CONNECTED_MAP.containsKey(side)) {
                            TileFloodGate floodGate = (TileFloodGate) tile;
                            if (!floodGate.openSides.remove(side)) {
                                floodGate.openSides.add(side);
                            }
                            floodGate.queue.clear();
                            floodGate.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        }
        return super.use(state, world, pos, player, hand, hit);
	}
	


}
