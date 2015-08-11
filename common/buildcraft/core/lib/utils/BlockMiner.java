package buildcraft.core.lib.utils;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.BuildCraftCore;
import buildcraft.core.proxy.CoreProxy;

public class BlockMiner {
	public interface OverrideDrops {
		void insertStack(ItemStack stack);
	}

	protected final World world;
	protected final Object o;
	protected final int x, y, z, minerId;

	private boolean hasMined, hasFailed;
	private int energyRequired, energyAccepted;

	public BlockMiner(World world, Object o, int x, int y, int z) {
		this.world = world;
		this.o = o;
		this.x = x;
		this.y = y;
		this.z = z;
		this.minerId = world.rand.nextInt();
	}

	public boolean hasMined() {
		return hasMined;
	}

	public boolean hasFailed() {
		return hasFailed;
	}

	public void mineStack(ItemStack stack) {
		if (o instanceof TileEntity) {
			TileEntity owner = (TileEntity) o;

			// First, try to add to a nearby chest
			stack.stackSize -= Utils.addToRandomInventoryAround(owner.getWorldObj(), owner.xCoord, owner.yCoord, owner.zCoord, stack);

			// Second, try to add to adjacent pipes
			if (stack.stackSize > 0) {
				stack.stackSize -= Utils.addToRandomInjectableAround(owner.getWorldObj(), owner.xCoord, owner.yCoord, owner.zCoord, ForgeDirection.UNKNOWN, stack);
			}

			// Lastly, throw the object away
			if (stack.stackSize > 0) {
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

				EntityItem entityitem = new EntityItem(owner.getWorldObj(), owner.xCoord + f, owner.yCoord + f1 + 0.5F, owner.zCoord + f2, stack);

				entityitem.lifespan = BuildCraftCore.itemLifespan * 20;
				entityitem.delayBeforeCanPickup = 10;

				float f3 = 0.05F;
				entityitem.motionX = (float) world.rand.nextGaussian() * f3;
				entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 1.0F;
				entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
				owner.getWorldObj().spawnEntityInWorld(entityitem);
			}
		} else if (o instanceof OverrideDrops) {
			((OverrideDrops) o).insertStack(stack);
		}
	}

	public void invalidate() {
		world.destroyBlockInWorldPartially(minerId, x, y, z, -1);
	}

	public int acceptEnergy(int offeredAmount) {
		if (BlockUtils.isUnbreakableBlock(world, x, y, z) || world.isAirBlock(x, y, z)) {
			hasFailed = true;
		}

		if (hasFailed) {
			world.destroyBlockInWorldPartially(minerId, x, y, z, -1);
			return 0;
		}

		energyRequired = BlockUtils.computeBlockBreakEnergy(world, x, y, z);

		int usedAmount = MathUtils.clamp(offeredAmount, -energyAccepted, Math.max(0, energyRequired - energyAccepted));
		energyAccepted += usedAmount;

		if (energyAccepted >= energyRequired) {
			world.destroyBlockInWorldPartially(minerId, x, y, z, -1);

			hasMined = true;

			Block block = world.getBlock(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);

			BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(x, y, z, world, block, meta,
					CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world).get());
			MinecraftForge.EVENT_BUS.post(breakEvent);

			if (!breakEvent.isCanceled()) {
				List<ItemStack> stacks = BlockUtils.getItemStackFromBlock((WorldServer) world, x, y, z);

				if (stacks != null) {
					for (ItemStack s : stacks) {
						if (s != null) {
							mineStack(s);
						}
					}
				}

				world.playAuxSFXAtEntity(
						null,
						2001,
						x, y, z,
						Block.getIdFromBlock(block)
								+ (meta << 12));

				Utils.preDestroyBlock(world, x, y, z);
				world.setBlockToAir(x, y, z);
			} else {
				hasFailed = true;
			}
		} else {
			world.destroyBlockInWorldPartially(minerId, x, y, z, MathUtils.clamp((int) Math.floor(energyAccepted * 10 / energyRequired), 0, 9));
		}
		return usedAmount;
	}

	public float getProgress() {
		return (float) energyAccepted / energyRequired;
	}
}
