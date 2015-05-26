package buildcraft.robotics.boards;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.statements.ActionRobotFilter;

public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {

	private BlockIndex blockFound;
	private ArrayList<Block> blockFilter = new ArrayList<Block>();
	private ArrayList<Integer> metaFilter = new ArrayList<Integer>();

	public BoardRobotGenericSearchBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	/**
	 * This function has to be derived in a thread safe manner, as it may be
	 * called from parallel jobs. In particular, world should not be directly
	 * used, only through WorldProperty class and subclasses.
	 */
	public abstract boolean isExpectedBlock(World world, int x, int y, int z);

	@Override
	public void update() {
		updateFilter();

		startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, new IBlockFilter() {
			@Override
			public boolean matches(World world, int x, int y, int z) {
				if (isExpectedBlock(world, x, y, z)
						&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))) {
					return matchesGateFilter(world, x, y, z);
				} else {
					return false;
				}
			}
		}));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchAndGotoBlock) {
			if (ai.success()) {
				blockFound = ((AIRobotSearchAndGotoBlock) ai).getBlockFound();
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	@Override
	public void end() {
		releaseBlockFound(true);
	}

	protected BlockIndex blockFound() {
		return blockFound;
	}

	protected void releaseBlockFound(boolean success) {
		if (blockFound != null) {
			// TODO: if !ai.success() -> can't break block, blacklist it
			robot.getRegistry().release(new ResourceIdBlock(blockFound));
			blockFound = null;
		}
	}

	public final void updateFilter() {
		blockFilter.clear();
		metaFilter.clear();

		for (StatementSlot slot : robot.getLinkedStation().getActiveActions()) {
			if (slot.statement instanceof ActionRobotFilter) {
				for (IStatementParameter p : slot.parameters) {
					if (p != null && p instanceof StatementParameterItemStack) {
						StatementParameterItemStack param = (StatementParameterItemStack) p;
						ItemStack stack = param.getItemStack();

						if (stack != null && stack.getItem() instanceof ItemBlock) {
							blockFilter.add(((ItemBlock) stack.getItem()).field_150939_a);
							metaFilter.add(stack.getItemDamage());
						}
					}
				}
			}
		}
	}

	protected boolean matchesGateFilter(World world, int x, int y, int z) {
		if (blockFilter.size() == 0) {
			return true;
		}

		Block block;
		int meta;
		synchronized (world) {
			block = world.getBlock(x, y, z);
			meta = world.getBlockMetadata(x, y, z);
		}

		for (int i = 0; i < blockFilter.size(); ++i) {
			if (blockFilter.get(i) == block && metaFilter.get(i) == meta) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockFound != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockFound.writeTo(sub);
			nbt.setTag("indexStored", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("indexStored")) {
			blockFound = new BlockIndex(nbt.getCompoundTag("indexStored"));
		}
	}

}