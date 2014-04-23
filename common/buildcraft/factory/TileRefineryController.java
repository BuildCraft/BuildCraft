package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketUpdate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileRefineryController extends TileMultiblockMaster {

	@NetworkData
	public int orientation = -1;

	@NetworkData
	public int length = 0;

	private boolean firstRun = true;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		orientation = nbt.getInteger("orientation");
		formed = nbt.getBoolean("formed");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("orientation", orientation);
		nbt.setBoolean("formed", formed);
	}

	@Override
	public void updateEntity() {
		if (worldObj != null && !worldObj.isRemote) {
			if (firstRun && formed) {
				formMultiblock();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				firstRun = false;
			}
		}
	}

	@Override
	public void validate() {
//		if (formed) {
//			formMultiblock(); // Try to form a multiblock when first loaded if been formed before
//		}
	}

	@Override
	public void onBlockActivated(EntityPlayer player) {
		ItemStack stack = player.getCurrentEquippedItem();

		if (!formed && !player.isSneaking() && stack != null && stack.getItem() instanceof IToolWrench) {
			IToolWrench wrench = (IToolWrench) stack.getItem();

			if (wrench.canWrench(player, xCoord, yCoord, zCoord)) {
				formMultiblock();
				wrench.wrenchUsed(player, xCoord, yCoord, zCoord);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	@Override
	public void formMultiblock() {
		// Orientation of controller determines multiblock orientation
		Position search = new Position(this);
		search.orientation = ForgeDirection.getOrientation(orientation);
		boolean foundEnd = false;
		int valveCount = 0;
		int frameCount = 0;
		int tankCount = 0;
		int heaterCount = 0;
		int length = 1; // Must be less than 9

		ForgeDirection forge_orientation = ForgeDirection.getOrientation(orientation);
		ForgeDirection left = forge_orientation.getOpposite().getRotation(ForgeDirection.UP);

		/* BEGIN FRONT CHECK */

		// Check bottom three blocks
		for (int i = -1; i <= 1; i++) {
			Block block = worldObj.getBlock(xCoord + left.offsetX * i, yCoord - 1, zCoord + left.offsetZ * i);
			int meta = worldObj.getBlockMetadata(xCoord + left.offsetX * i, yCoord - 1, zCoord + left.offsetZ * i);

			if (block == BuildCraftFactory.refineryComponent) {
				if (meta == BlockRefineryComponent.HEATER) {
					heaterCount++;
				} else if (meta == BlockRefineryComponent.VALVE) {
					valveCount++;
				}
			}
		}

		// Check middle three blocks
		for (int i = -1; i <= 1; i++) {
			Block block = worldObj.getBlock(xCoord + left.offsetX * i, yCoord, zCoord + left.offsetZ * i);
			int meta = worldObj.getBlockMetadata(xCoord + left.offsetX * i, yCoord, zCoord + left.offsetZ * i);

			if (i != 0) {
				if (block == BuildCraftFactory.refineryComponent && meta == BlockRefineryComponent.FRAME) {
					frameCount++;
				}
			}
		}

		// Check top three blocks
		for (int i = -1; i <= 1; i++) {
			Block block = worldObj.getBlock(xCoord + left.offsetX * i, yCoord + 1, zCoord + left.offsetZ * i);
			int meta = worldObj.getBlockMetadata(xCoord + left.offsetX * i, yCoord + 1, zCoord + left.offsetZ * i);

			if (block == BuildCraftFactory.refineryComponent && meta == BlockRefineryComponent.FRAME) {
				frameCount++;
			}
		}

		/* END FRONT CHECK */

		// Then iterate backwards
		while (!foundEnd) {
			search.moveForwards(1); // Orientation util method is technically backwards ;)
			length++;

			if (length > 11) {
				break;
			}

			// Boiler is hollow, so its a good method to see where we are
			if (!worldObj.isAirBlock((int) search.x, (int) search.y, (int) search.z) && length > 1) {
				foundEnd = true;

				// Check for existance of middle tank block, as this should be the end
				Block block = worldObj.getBlock((int) search.x, (int) search.y, (int) search.z);
				int meta = worldObj.getBlockMetadata((int) search.x, (int) search.y, (int) search.z);

				if (block == BuildCraftFactory.refineryComponent && meta == BlockRefineryComponent.TANK) {
					tankCount++;
				}
			}

			// Check bottom three blocks
			for (int i = -1; i <= 1; i++) {
				Block block = worldObj.getBlock((int) search.x + left.offsetX * i, (int) search.y - 1, (int) search.z + left.offsetZ * i);
				int meta = worldObj.getBlockMetadata((int) search.x + left.offsetX * i, (int) search.y - 1, (int) search.z + left.offsetZ * i);

				if (block == BuildCraftFactory.refineryComponent) {
					if (meta == BlockRefineryComponent.HEATER) {
						heaterCount++;
					} else if (meta == BlockRefineryComponent.VALVE) {
						valveCount++;
					}
				}
			}

			// Check middle three blocks
			for (int i = -1; i <= 1; i++) {
				Block block = worldObj.getBlock((int) search.x + left.offsetX * i, yCoord, (int) search.z + left.offsetZ * i);
				int meta = worldObj.getBlockMetadata((int) search.x + left.offsetX * i, yCoord, (int) search.z + left.offsetZ * i);

				if (i != 0) {
					if (block == BuildCraftFactory.refineryComponent && meta == BlockRefineryComponent.TANK) {
						tankCount++;
					}
				}
			}

			// Check top three blocks
			for (int i = -1; i <= 1; i++) {
				Block block = worldObj.getBlock((int) search.x + left.offsetX * i, yCoord + 1, (int) search.z + left.offsetZ * i);
				int meta = worldObj.getBlockMetadata((int) search.x + left.offsetX * i, yCoord + 1, (int) search.z + left.offsetZ * i);

				if (block == BuildCraftFactory.refineryComponent && meta == BlockRefineryComponent.TANK) {
					tankCount++;
				}
			}
		}

		int requiredValveCount = 2;
		int requiredHeaterCount = 3 * length - valveCount; // Three heaters form the bottom, minus the required valves
		int requiredFrameCount = 5; // Five frames surrounding the controller
		int requiredTankCount = (5 * (length - 1)) + 1; // Tank blocks surrounding middle, plus plug at end

		if (valveCount != requiredValveCount || heaterCount != requiredHeaterCount || frameCount != requiredFrameCount || tankCount != requiredTankCount) {
			// Indicate failure in some way
		} else {
			this.length = length;
			formed = true;

			// Iterate through all blocks once more to update state
			for (int i = 0; i < length; i++) {
				for (int j = -1; j <= 1; j++) {
					for (int k = -1; k <= 1; k++) {
						int x = xCoord + (forge_orientation.offsetX * i) + (left.offsetX * k);
						int y = yCoord + j;
						int z = zCoord + (forge_orientation.offsetZ * i) + (left.offsetZ * k);

						TileEntity tile = worldObj.getTileEntity(x, y, z);

						if (tile != null && tile instanceof TileMultiblockSlave) {
							((TileMultiblockSlave) tile).setMaster(new Position(this));
						}
					}
				}
			}
		}
	}

	@Override
	public void deformMultiblock() {
		super.deformMultiblock();

		ForgeDirection forge_orientation = ForgeDirection.getOrientation(orientation);
		ForgeDirection left = forge_orientation.getOpposite().getRotation(ForgeDirection.UP);

		for (int i = 0; i < length; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					int x = xCoord + (forge_orientation.offsetX * i) + (left.offsetX * i);
					int y = yCoord + j;
					int z = zCoord + (forge_orientation.offsetZ * k) + (left.offsetZ * k);

					TileEntity tile = worldObj.getTileEntity(x, y, z);

					if (tile != null && tile instanceof TileMultiblockSlave) {
						((TileMultiblockSlave) tile).clear();
					}
				}
			}
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		super.postPacketHandling(packet);

		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
}
