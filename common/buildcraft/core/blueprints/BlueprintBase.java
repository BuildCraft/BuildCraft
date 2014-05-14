/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.blueprints.Translation;
import buildcraft.api.core.BCLog;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.core.Box;
import buildcraft.core.Version;

public abstract class BlueprintBase {

	public SchematicBlockBase[][][] contents;
	public int anchorX, anchorY, anchorZ;
	public int sizeX, sizeY, sizeZ;
	public BlueprintId id = new BlueprintId();
	public String author;
	public boolean rotate = true;
	public boolean excavate = true;
	public BuildingPermission buildingPermission = BuildingPermission.ALL;
	public boolean isComplete = true;

	protected MappingRegistry mapping = new MappingRegistry();

	private String version = "";
	private ComputeDataThread computeData;
	private byte [] data;

	public BlueprintBase() {
	}

	public BlueprintBase(int sizeX, int sizeY, int sizeZ) {
		contents = new SchematicBlockBase[sizeX][sizeY][sizeZ];

		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;

		anchorX = 0;
		anchorY = 0;
		anchorZ = 0;
	}

	public void translateToBlueprint(Translation transform) {
		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					if (contents [x][y][z] != null) {
						contents[x][y][z].translateToBlueprint(transform);
					}
				}
			}
		}
	}

	public void translateToWorld(Translation transform) {
		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					if (contents [x][y][z] != null) {
						contents[x][y][z].translateToWorld(transform);
					}
				}
			}
		}
	}

	public void rotateLeft(BptContext context) {
		SchematicBlockBase[][][] newContents = new SchematicBlockBase[sizeZ][sizeY][sizeX];

		for (int x = 0; x < sizeZ; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeX; ++z) {
					newContents[x][y][z] = contents[z][y][(sizeZ - 1) - x];

					if (newContents[x][y][z] != null) {
						try {
							newContents[x][y][z].rotateLeft(context);
						} catch (Throwable t) {
							// Defensive code against errors in implementers
							t.printStackTrace();
							BCLog.logger.throwing("BptBase", "rotateLeft", t);
						}
					}
				}
			}
		}

		int newAnchorX, newAnchorY, newAnchorZ;

		newAnchorX = (sizeZ - 1) - anchorZ;
		newAnchorY = anchorY;
		newAnchorZ = anchorX;

		contents = newContents;
		int tmp = sizeX;
		sizeX = sizeZ;
		sizeZ = tmp;

		anchorX = newAnchorX;
		anchorY = newAnchorY;
		anchorZ = newAnchorZ;

		context.rotateLeft();
	}

	public void writeToNBT (NBTTagCompound nbt) {
		nbt.setString("version", Version.VERSION);

		if (this instanceof Template) {
			nbt.setString("kind", "template");
		} else {
			nbt.setString("kind", "blueprint");
		}

		nbt.setInteger("sizeX", sizeX);
		nbt.setInteger("sizeY", sizeY);
		nbt.setInteger("sizeZ", sizeZ);
		nbt.setInteger("anchorX", anchorX);
		nbt.setInteger("anchorY", anchorY);
		nbt.setInteger("anchorZ", anchorZ);
		nbt.setBoolean("rotate", rotate);
		nbt.setBoolean("excavate", excavate);

		if (author != null) {
			nbt.setString("author", author);
		}

		saveContents(nbt);
	}

	public static BlueprintBase loadBluePrint(NBTTagCompound nbt) {
		String kind = nbt.getString("kind");

		BlueprintBase bpt;

		if ("template".equals(kind)) {
			bpt = new Template ();
		} else {
			bpt = new Blueprint();
		}

		bpt.readFromNBT(nbt);

		return bpt;
	}

	public void readFromNBT (NBTTagCompound nbt) {
		BlueprintBase result = null;

		String versionRead = nbt.getString("version");

		sizeX = nbt.getInteger("sizeX");
		sizeY = nbt.getInteger("sizeY");
		sizeZ = nbt.getInteger("sizeZ");
		anchorX = nbt.getInteger("anchorX");
		anchorY = nbt.getInteger("anchorY");
		anchorZ = nbt.getInteger("anchorZ");

		author = nbt.getString("author");

		if (nbt.hasKey("rotate")) {
			rotate = nbt.getBoolean("rotate");
		} else {
			rotate = true;
		}

		if (nbt.hasKey("excavate")) {
			excavate = nbt.getBoolean("excavate");
		} else {
			excavate = true;
		}

		contents = new SchematicBlockBase [sizeX][sizeY][sizeZ];

		try {
			loadContents (nbt);
		} catch (BptError e) {
			e.printStackTrace();
		}
	}

	protected void copyTo(BlueprintBase base) {

	}

	public Box getBoxForPos(int x, int y, int z) {
		int xMin = x - anchorX;
		int yMin = y - anchorY;
		int zMin = z - anchorZ;
		int xMax = x + sizeX - anchorX - 1;
		int yMax = y + sizeY - anchorY - 1;
		int zMax = z + sizeZ - anchorZ - 1;

		Box res = new Box();
		res.initialize(xMin, yMin, zMin, xMax, yMax, zMax);
		res.reorder();

		return res;
	}

	public BptContext getContext (World world, Box box) {
		return new BptContext(world, box, mapping);
	}

	class ComputeDataThread extends Thread {
		public NBTTagCompound nbt;

		@Override
		public void run () {
			try {
				BlueprintBase.this.setData(CompressedStreamTools.compress(nbt));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * This function will return the binary data associated to this blueprint.
	 * This data is computed asynchronously. If the data is not yet available,
	 * null will be returned.
	 */
	public synchronized byte [] getData () {
		if (data != null) {
			return data;
		} else if (computeData == null) {
			computeData = new ComputeDataThread();
			computeData.nbt = new NBTTagCompound();
			writeToNBT(computeData.nbt);
			computeData.start();
		}

		return null;
	}

	public synchronized void setData (byte [] b) {
		data = b;
	}

	public abstract void loadContents(NBTTagCompound nbt) throws BptError;

	public abstract void saveContents(NBTTagCompound nbt);

	public abstract void readFromWorld(IBuilderContext context, TileEntity anchorTile, int x, int y, int z);

	public abstract ItemStack getStack ();

	public void readEntitiesFromWorld(IBuilderContext context, TileEntity anchorTile) {

	}
}
