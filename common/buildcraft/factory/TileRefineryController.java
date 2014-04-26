package buildcraft.factory;

import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.core.fluids.Tank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketUpdate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class TileRefineryController extends TileMultiblockMaster implements IFluidHandler {

	private static final int MIN_LENGTH = 3;
	private static final int MAX_LENGTH = 11;

	@NetworkData
	public int orientation = -1;

	@NetworkData
	public int length = 0;

	//TODO Variable size?
	private Tank tankOil = new Tank("oil", FluidContainerRegistry.BUCKET_VOLUME * 10, this);
	private Tank tankFuel = new Tank("fuel", FluidContainerRegistry.BUCKET_VOLUME * 10, this);
	private TankManager manager = new TankManager();

	public TileRefineryController() {
		manager.add(tankOil);
		manager.add(tankFuel);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		orientation = nbt.getInteger("orientation");
		manager.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("orientation", orientation);
		manager.writeToNBT(nbt);
	}

	@Override
	public void formMultiblock(EntityPlayer player) {
		// Orientation of controller determines multiblock orientation
		Position search = new Position(this);
		search.orientation = ForgeDirection.getOrientation(orientation);
		boolean foundEnd = false;
		int valveCount = 0;
		int hatchCount = 0;
		int frameCount = 0;
		int tankCount = 0;
		int heaterCount = 0;
		int length = 1; // Must be less than MAX_LENGTH

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
				} else if (meta == BlockRefineryComponent.VALVE_STEEL) {
					valveCount++;
				} else if (meta == BlockRefineryComponent.HATCH) {
					hatchCount++;
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

			if (length > MAX_LENGTH) {
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
					} else if (meta == BlockRefineryComponent.VALVE_STEEL) {
						valveCount++;
					} else if (meta == BlockRefineryComponent.HATCH) {
						hatchCount++;
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

		int requiredHatchCount = 1;
		int requiredValveCount = 2;
		int requiredHeaterCount = 3 * length - requiredValveCount - requiredHatchCount; // Three heaters form the bottom, minus the required valves
		int requiredFrameCount = 5; // Five frames surrounding the controller
		int requiredTankCount = (5 * (length - 1)) + 1; // Tank blocks surrounding middle, plus plug at end

		boolean minLength = length >= MIN_LENGTH;
		boolean maxLength = length <= MAX_LENGTH;
		boolean hatches = hatchCount == requiredHatchCount;
		boolean valves = valveCount == requiredValveCount;
		boolean heaters = heaterCount == requiredHeaterCount;
		boolean frames = frameCount == requiredFrameCount;
		boolean tanks = tankCount == requiredTankCount;

		if (minLength && maxLength && hatches && valves && heaters && frames && tanks) {
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
		} else {
			if (player != null) {
				boilerHeader(player);

				if (!minLength) {
					boilerError(player, "length_min", MIN_LENGTH, length);
				}

				if (!maxLength) {
					boilerError(player, "length_max", MAX_LENGTH, length);
				}

				if (!hatches) {
					boilerError(player, "hatch", requiredHatchCount, hatchCount);
				}

				if (!valves) {
					boilerError(player, "valve", requiredValveCount, valveCount);
				}

				if (!heaters) {
					boilerError(player, "heater", requiredHeaterCount, heaterCount);
				}

				if (!frames) {
					boilerError(player, "frame", requiredFrameCount, frameCount);
				}

				if (!tanks) {
					boilerError(player, "tank", requiredTankCount, tankCount);
				}
			}
		}
	}

	private void boilerHeader(EntityPlayer player) {
		player.addChatComponentMessage(new ChatComponentTranslation("chat.boiler.error.header"));
	}

	private void boilerError(EntityPlayer player, String type, Object... args) {
		player.addChatComponentMessage(new ChatComponentText(" - ").appendSibling(new ChatComponentTranslation("chat.boiler.error." + type, args)));
	}

	@Override
	public void deformMultiblock() {
		super.deformMultiblock();

		ForgeDirection forge_orientation = ForgeDirection.getOrientation(orientation);
		ForgeDirection left = forge_orientation.getOpposite().getRotation(ForgeDirection.UP);

		for (int i = 0; i < length; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					int x = xCoord + (forge_orientation.offsetX * i) + (left.offsetX * k);
					int y = yCoord + j;
					int z = zCoord + (forge_orientation.offsetZ * i) + (left.offsetZ * k);

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

	/* IFLUIDHANDLER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource.getFluid() == BuildCraftEnergy.fluidOil) {
			return tankOil.fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return tankFuel.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tankFuel.drain(maxDrain, doDrain);
	}

	// These are false as to prevent pipes/other blocks from connecting to the controller
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tankOil.getInfo(), tankFuel.getInfo()};
	}

}
