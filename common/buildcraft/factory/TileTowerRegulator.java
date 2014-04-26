package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketUpdate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.ForgeDirection;

public class TileTowerRegulator extends TileMultiblockMaster {

	public static final int MIN_HEIGHT = 4;
	public static final int MAX_HEIGHT = 12;

	@NetworkData
	public int height = 0;

	@NetworkData
	public int size = 0;

	@Override
	public void formMultiblock(EntityPlayer player) {
		int width = 0;
		int depth = 0;
		int height = 0;

		/* SIZE DETERMINATION */
		int north = -1;
		int south = -1;
		int east = -1;
		int west = -1;

		for (int i = 2; i <= 5; i++) {
			int searchX = xCoord;
			int searchY = yCoord;
			int searchZ = zCoord;

			ForgeDirection side = ForgeDirection.getOrientation(i);

			while (!worldObj.isAirBlock(searchX, searchY, searchZ)) {
				searchX += side.offsetX;
				searchZ += side.offsetZ;

				switch (side) {
					case NORTH:
						north++;
						break;
					case SOUTH:
						south++;
						break;
					case EAST:
						east++;
						break;
					case WEST:
						west++;
						break;
				}
			}
		}

		// Scan up until finding the top of the tower/hit the height limit
		while (height < MAX_HEIGHT) {
			height++;

			if (worldObj.getBlock(xCoord, yCoord + height, zCoord) == BuildCraftFactory.refineryComponent && worldObj.getBlockMetadata(xCoord, yCoord + height, zCoord) == BlockRefineryComponent.TANK) {
				height++;
				break;
			}
		}

		// Go through each direction and pick the shortest out of them
		int size = 5;
		for (int i = 2; i <= 5; i++) {
			ForgeDirection side = ForgeDirection.getOrientation(i);
			switch (side) {
				case NORTH:
					if (north < size) size = north;
					break;
				case SOUTH:
					if (south < size) size = south;
					break;
				case EAST:
					if (east < size) size = east;
					break;
				case WEST:
					if (west < size) size = west;
					break;
			}
		}

		north = south = west = east = size;

		width = east + west + 1;
		depth = north + south + 1;

		// The above sizes are pretty rough. Now we go through and verify everything
		int filterLayerCount = 0;
		for (int i = 1; i < height - 1; i++) {
			if (i % 2 != 0) {
				filterLayerCount++;
			}
		}

		int frameCount = 0;
		int valveCount = 0;
		int tankCount = 0;
		int filterCount = 0;

		for (int i = -north; i <= south; i++) {
			for (int j = -east; j <= west; j++) {
				for (int k = 0; k < height; k++) {
					Block block = worldObj.getBlock(xCoord + i, yCoord + k, zCoord + j);
					int meta = worldObj.getBlockMetadata(xCoord + i, yCoord + k, zCoord + j);

					if (k == 0) {
						if (block == BuildCraftFactory.refineryComponent) {
							if (meta == BlockRefineryComponent.FRAME) {
								frameCount++;
							} else if (meta == BlockRefineryComponent.VALVE_STEEL) {
								valveCount++;
							}
						}
					} else {
						if (k % 2 != 0) {
							if (block == BuildCraftFactory.refineryComponent) {
								if (meta == BlockRefineryComponent.TANK) {
									tankCount++;
								} else if (meta == BlockRefineryComponent.FILTER) {
									filterCount++;
								} else if (meta == BlockRefineryComponent.VALVE_IRON) {
									valveCount++;
								}
							}
						} else {
							if (block == BuildCraftFactory.refineryComponent) {
								if (meta == BlockRefineryComponent.TANK) {
									tankCount++;
								}
							}
						}
					}
				}
			}
		}

		int requiredValveCount = 1 + filterLayerCount;
		int requiredFrameCount = (width * depth) - 2;
		int requiredTankCount = (width * depth) + ((((width * 2 + depth * 2) - 4) * (height - 2)) - filterLayerCount);
		int requiredFilterCount = ((width - 2) * (depth - 2)) * filterLayerCount;

//		System.out.println("VALVE: " + valveCount + ", " + requiredValveCount);
//		System.out.println("FRAME: " + frameCount + ", " + requiredFrameCount);
//		System.out.println("TANK: " + tankCount + ", " + requiredTankCount);
//		System.out.println("FILTER: " + filterCount + ", " + requiredFilterCount);

		boolean minHeight = height >= MIN_HEIGHT;
		boolean maxHeight = height <= MAX_HEIGHT;
		boolean validHeight = height == 5 || height == 7 || height == 9 || height == 11;
		boolean valves = valveCount == requiredValveCount;
		boolean frames = frameCount == requiredFrameCount;
		boolean tanks = tankCount == requiredTankCount;
		boolean filters = filterCount == requiredFilterCount;

		if (minHeight && maxHeight && validHeight && valves && frames && tanks && filters) {
			this.height = height;
			this.size = width;
			formed = true;

			int scan_size = (int) Math.ceil((this.size / 2));

			for (int i = -scan_size; i <= scan_size; i++) {
				for (int j = -scan_size; j <= scan_size; j++) {
					for (int k = 0; k < height; k++) {
						int x = xCoord + i;
						int y = yCoord + k;
						int z = zCoord + j;

						TileEntity tile = worldObj.getTileEntity(x, y, z);

						if (tile != null && tile instanceof TileMultiblockSlave) {
							((TileMultiblockSlave) tile).setMaster(new Position(this));
						}
					}
				}
			}
		} else {
			if (player != null) {
				towerHeader(player);

				if (!minHeight) {
					towerError(player, "height_invalid", MIN_HEIGHT, height);
				}

				if (!maxHeight) {
					towerError(player, "height_invalid", MIN_HEIGHT, height);
				}

				if (!validHeight) {
					towerError(player, "height_invalid");
				}

				if (!valves) {
					towerError(player, "valve", requiredValveCount, valveCount);
				}

				if (!frames) {
					towerError(player, "frame", requiredFrameCount, frameCount);
				}

				if (!tanks) {
					towerError(player, "tank", requiredTankCount, tankCount);
				}

				if (!filters) {
					towerError(player, "filter", requiredFilterCount, filterCount);
				}
			}
		}
	}

	@Override
	public void deformMultiblock() {
		formed = false;

		int size = (int) Math.ceil((this.size / 2));

		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				for (int k = 0; k < height; k++) {
					int x = xCoord + i;
					int y = yCoord + k;
					int z = zCoord + j;

					TileEntity tile = worldObj.getTileEntity(x, y, z);

					if (tile != null && tile instanceof TileMultiblockSlave) {
						((TileMultiblockSlave) tile).clear();
					}
				}
			}
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	private void towerHeader(EntityPlayer player) {
		player.addChatComponentMessage(new ChatComponentTranslation("chat.tower.error.header"));
	}

	private void towerError(EntityPlayer player, String type, Object... args) {
		player.addChatComponentMessage(new ChatComponentText(" - ").appendSibling(new ChatComponentTranslation("chat.tower.error." + type, args)));
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
