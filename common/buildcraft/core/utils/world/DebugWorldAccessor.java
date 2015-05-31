package buildcraft.core.utils.world;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

import com.google.common.collect.Lists;

public class DebugWorldAccessor {
	private static final DebugBlockAccessor debugWorld;
	private static final List<IBlockState> validStates = Lists.newArrayList();

	static {
		for (Object obj : Block.blockRegistry) {
			Block block = (Block) obj;
			validStates.addAll(block.getBlockState().getValidStates());
		}

		debugWorld = new DebugBlockAccessor(validStates);
	}

	public static DebugBlockAccessor getDebugWorld() {
		return debugWorld;
	}

	public static BlockPos getPositionForBlockState(IBlockState state) {
		return debugWorld.getBlockPos(state);
	}
}
