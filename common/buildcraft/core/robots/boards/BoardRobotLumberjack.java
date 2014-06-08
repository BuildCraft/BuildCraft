package buildcraft.core.robots.boards;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.api.boards.IRedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.BlockIndex;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.RobotAIMoveTo;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.IPathFound;
import buildcraft.core.utils.PathFinding;

public class BoardRobotLumberjack implements IRedstoneBoardRobot<EntityRobot> {

	private static enum Stages {
		LOOK_FOR_WOOD, GO_TO_WOOD, CUT_WOOD
	};

	private NBTTagCompound data;
	private RedstoneBoardNBT board;
	private int range;
	private boolean initialized = false;
	private PathFinding woodScanner = null;
	private Stages stage = Stages.LOOK_FOR_WOOD;
	private BlockIndex woodToChop;
	private float blockDamage;

	public BoardRobotLumberjack(NBTTagCompound nbt) {
		data = nbt;

		board = RedstoneBoardRegistry.instance.getRedstoneBoard(nbt);
	}

	@Override
	public void updateBoard(EntityRobot robot) {
		if (robot.worldObj.isRemote) {
			return;
		}

		if (!initialized) {
			range = data.getInteger("range");
			robot.setItemInUse(new ItemStack(Items.wooden_axe));

			initialized = true;
		}

		if (stage == Stages.LOOK_FOR_WOOD) {
			if (woodScanner == null) {
				woodScanner = new PathFinding(robot.worldObj, new BlockIndex(robot), new IPathFound() {
					@Override
					public boolean endReached(IBlockAccess world, int x, int y, int z) {
						return world.getBlock(x, y, z) == Blocks.log || world.getBlock(x, y, z) == Blocks.log2;
					}
				});
			} else {
				woodScanner.iterate(PathFinding.PATH_ITERATIONS);

				if (woodScanner.isDone()) {
					LinkedList<BlockIndex> path = woodScanner.getResult();
					woodToChop = path.removeLast();
					robot.setMainAI(new RobotAIMoveTo(robot, path));
					stage = Stages.GO_TO_WOOD;
				}
			}
		} else if (stage == Stages.GO_TO_WOOD) {
			if (robot.currentAI.isDone()) {
				stage = Stages.CUT_WOOD;
				blockDamage = 0;
			}
		} else if (stage == Stages.CUT_WOOD) {
			Block block = robot.worldObj.getBlock(woodToChop.x, woodToChop.y, woodToChop.z);
			int meta = robot.worldObj.getBlockMetadata(woodToChop.x, woodToChop.y, woodToChop.z);
			float hardness = block.getBlockHardness(robot.worldObj, woodToChop.x, woodToChop.y, woodToChop.z);
			float speed = getBreakSpeed(robot, robot.itemInUse, block, meta);
			blockDamage += speed / hardness / 30F;

            if (blockDamage > 1.0F) {
				robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), woodToChop.x,
						woodToChop.y, woodToChop.z, -1);
            	blockDamage = 0;
				BlockUtil.breakBlock((WorldServer) robot.worldObj, woodToChop.x, woodToChop.y, woodToChop.z, 6000);
				robot.worldObj.setBlockToAir(woodToChop.x, woodToChop.y, woodToChop.z);
				stage = Stages.LOOK_FOR_WOOD;
				woodToChop = null;
				woodScanner = null;
			} else {
				robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), woodToChop.x,
						woodToChop.y, woodToChop.z, (int) (blockDamage * 10.0F) - 1);
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotLumberjackNBT.instance;
	}

	private float getBreakSpeed(EntityRobot robot, ItemStack usingItem, Block block, int meta) {
		ItemStack stack = usingItem;
		float f = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, block, meta);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(robot);
			ItemStack itemstack = usingItem;

			if (i > 0 && itemstack != null) {
				float f1 = i * i + 1;

				boolean canHarvest = ForgeHooks.canToolHarvestBlock(block, meta, itemstack);

				if (!canHarvest && f <= 1.0F) {
					f += f1 * 0.08F;
				} else {
					f += f1;
				}
			}
		}

		return f;
	}
}
