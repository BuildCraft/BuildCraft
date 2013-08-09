package buildcraft.core.fluids;

import buildcraft.core.TileBuffer;
import buildcraft.core.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class FluidUtils {

	public static boolean handleRightClick(IFluidHandler tank, ForgeDirection side, EntityPlayer player, boolean fill, boolean drain) {
		if (player == null) {
			return false;
		}
		ItemStack current = player.inventory.getCurrentItem();
		if (current != null) {

			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);

			if (fill && liquid != null) {
				int used = tank.fill(side, liquid, true);

				if (used > 0) {
					if (!player.capabilities.isCreativeMode) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, Utils.consumeItem(current));
					}
					return true;
				}

			} else if (drain) {

				FluidStack available = tank.drain(side, Integer.MAX_VALUE, false);
				if (available != null) {
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

					liquid = FluidContainerRegistry.getFluidForFilledItem(filled);
					if (liquid != null) {

						if (current.stackSize > 1) {
							if (!player.inventory.addItemStackToInventory(filled)) {
								return false;
							} else {
								player.inventory.setInventorySlotContents(player.inventory.currentItem, Utils.consumeItem(current));
							}
						} else {
							player.inventory.setInventorySlotContents(player.inventory.currentItem, Utils.consumeItem(current));
							player.inventory.setInventorySlotContents(player.inventory.currentItem, filled);
						}

						tank.drain(side, liquid.amount, true);
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int getFluidBlockId(Fluid fluid, boolean moving) {
		if (fluid == FluidRegistry.WATER)
			return moving ? Block.waterMoving.blockID : Block.waterStill.blockID;
		if (fluid == FluidRegistry.LAVA)
			return moving ? Block.lavaMoving.blockID : Block.lavaStill.blockID;
		return fluid.getBlockID();
	}

	public static void pushFluidToConsumers(IFluidTank tank, int flowCap, TileBuffer[] tileBuffer) {
		int amountToPush = flowCap;
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			FluidStack fluidStack = tank.drain(amountToPush, false);
			if (fluidStack != null && fluidStack.amount > 0) {
				TileEntity tile = tileBuffer[side.ordinal()].getTile();
				if (tile instanceof IFluidHandler) {
					int used = ((IFluidHandler) tile).fill(side.getOpposite(), fluidStack, true);
					if (used > 0) {
						amountToPush -= used;
						tank.drain(used, true);
						if (amountToPush <= 0)
							return;
					}
				}
			}
		}
	}
}
