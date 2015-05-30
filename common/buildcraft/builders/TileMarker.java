/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.core.EntityBlock;
import buildcraft.core.LaserKind;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class TileMarker extends TileBuildCraft implements IAreaProvider {
	private static int maxSize = 64;

	public static class TileWrapper implements ISerializable {

		public BlockPos pos;
		private TileMarker marker;

		public TileWrapper() {
		}

		public TileWrapper(BlockPos pos) {
			this.pos = pos;
		}

		public boolean isSet() {
			return pos != null;
		}

		public TileMarker getMarker(World world) {
			if (!isSet()) {
				return null;
			}

			if (marker == null) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile instanceof TileMarker) {
					marker = (TileMarker) tile;
				}
			}

			return marker;
		}

		public void reset() {
			pos = null;
		}

		@Override
		public void readData(ByteBuf stream) {
			if (stream.readBoolean()) {
				pos = Utils.readBlockPos(stream);
			}
		}

		@Override
		public void writeData(ByteBuf stream) {
			stream.writeBoolean(pos != null);
			if (pos != null) {
				Utils.writeBlockPos(stream, pos);
			}
		}
	}

	public static class Origin implements ISerializable {
		public TileWrapper vectO = new TileWrapper();
		public TileWrapper[] vect = {new TileWrapper(), new TileWrapper(), new TileWrapper()};
		public int xMin, yMin, zMin, xMax, yMax, zMax;

		public boolean isSet() {
			return vectO.isSet();
		}

		@Override
		public void writeData(ByteBuf stream) {
			vectO.writeData(stream);
			for (TileWrapper tw : vect) {
				tw.writeData(stream);
			}
			stream.writeInt(xMin);
			stream.writeShort(yMin);
			stream.writeInt(zMin);
			stream.writeInt(xMax);
			stream.writeShort(yMax);
			stream.writeInt(zMax);
		}

		@Override
		public void readData(ByteBuf stream) {
			vectO.readData(stream);
			for (TileWrapper tw : vect) {
				tw.readData(stream);
			}
			xMin = stream.readInt();
			yMin = stream.readShort();
			zMin = stream.readInt();
			xMax = stream.readInt();
			yMax = stream.readShort();
			zMax = stream.readInt();
		}
	}

	public Origin origin = new Origin();
	public boolean showSignals = false;

	private Position initVectO;
	private Position[] initVect;
	private EntityBlock[] lasers;
	private EntityBlock[] signals;

	public void updateSignals() {
		if (!worldObj.isRemote) {
			showSignals = worldObj.isBlockPowered(pos);
			sendNetworkUpdate();
		}
	}

	private void switchSignals() {
		if (signals != null) {
			for (EntityBlock b : signals) {
				if (b != null) {
					CoreProxy.proxy.removeEntity(b);
				}
			}
			signals = null;
		}
		if (showSignals) {
			signals = new EntityBlock[6];
			if (!origin.isSet() || !origin.vect[0].isSet()) {
				signals[0] = Utils.createLaser(worldObj, new Position(pos.getX(), pos.getY(), pos.getZ()), new Position(pos.getX() + maxSize - 1, pos.getY(), pos.getZ()),
						LaserKind.Blue);
				signals[1] = Utils.createLaser(worldObj, new Position(pos.getX() - maxSize + 1, pos.getY(), pos.getZ()), new Position(pos.getX(), pos.getY(), pos.getZ()),
						LaserKind.Blue);
			}

			if (!origin.isSet() || !origin.vect[1].isSet()) {
				signals[2] = Utils.createLaser(worldObj, new Position(pos.getX(), pos.getY(), pos.getZ()), new Position(pos.getX(), pos.getY() + maxSize - 1, pos.getZ()),
						LaserKind.Blue);
				signals[3] = Utils.createLaser(worldObj, new Position(pos.getX(), pos.getY() - maxSize + 1, pos.getZ()), new Position(pos.getX(), pos.getY(), pos.getZ()),
						LaserKind.Blue);
			}

			if (!origin.isSet() || !origin.vect[2].isSet()) {
				signals[4] = Utils.createLaser(worldObj, new Position(pos.getX(), pos.getY(), pos.getZ()), new Position(pos.getX(), pos.getY(), pos.getZ() + maxSize - 1),
						LaserKind.Blue);
				signals[5] = Utils.createLaser(worldObj, new Position(pos.getX(), pos.getY(), pos.getZ() - maxSize + 1), new Position(pos.getX(), pos.getY(), pos.getZ()),
						LaserKind.Blue);
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		updateSignals();

		if (initVectO != null) {
			origin = new Origin();

			origin.vectO = new TileWrapper(initVectO.toBlockPos());

			for (int i = 0; i < 3; ++i) {
				if (initVect[i] != null) {
					linkTo((TileMarker) worldObj.getTileEntity(initVect[i].toBlockPos()), i);
				}
			}
		}
	}

	public void tryConnection() {
		if (worldObj.isRemote) {
			return;
		}

		for (int j = 0; j < 3; ++j) {
			if (!origin.isSet() || !origin.vect[j].isSet()) {
				setVect(j);
			}
		}

		sendNetworkUpdate();
	}

	void setVect(int n) {
		BlockPos coords = pos;

		if (!origin.isSet() || !origin.vect[n].isSet()) {
			for (int j = 1; j < maxSize; ++j) {
				coords.add(n == 0 ? j : 0, n == 1 ? j : 0, n == 2 ? j : 0);

				Block block = worldObj.getBlockState(coords).getBlock();

				if (block == BuildCraftBuilders.markerBlock) {
					TileMarker marker = (TileMarker) worldObj.getTileEntity(coords);

					if (linkTo(marker, n)) {
						break;
					}
				}

				coords.add(n == 0 ? (-2 * j) : 0, n == 1 ? (-2 * j) : 0, n == 2 ? (-2 * j) : 0);

				block = worldObj.getBlockState(coords).getBlock();

				if (block == BuildCraftBuilders.markerBlock) {
					TileMarker marker = (TileMarker) worldObj.getTileEntity(coords);

					if (linkTo(marker, n)) {
						break;
					}
				}

				coords.add(n == 0 ? j : 0, n == 1 ? j : 0, n == 2 ? j : 0);
			}
		}
	}

	private boolean linkTo(TileMarker marker, int n) {
		if (marker == null) {
			return false;
		}

		if (origin.isSet() && marker.origin.isSet()) {
			return false;
		}

		if (!origin.isSet() && !marker.origin.isSet()) {
			origin = new Origin();
			marker.origin = origin;
			origin.vectO = new TileWrapper(pos);
			origin.vect[n] = new TileWrapper(marker.pos);
		} else if (!origin.isSet()) {
			origin = marker.origin;
			origin.vect[n] = new TileWrapper(pos);
		} else {
			marker.origin = origin;
			origin.vect[n] = new TileWrapper(marker.pos);
		}

		origin.vectO.getMarker(worldObj).createLasers();
		updateSignals();
		marker.updateSignals();

		return true;
	}

	private void createLasers() {
		if (lasers != null) {
			for (EntityBlock entity : lasers) {
				if (entity != null) {
					CoreProxy.proxy.removeEntity(entity);
				}
			}
		}

		lasers = new EntityBlock[12];
		Origin o = origin;
		
		if (origin.vectO.pos == null) {
			origin.vectO.pos = getPos();
		}

		if (!origin.vect[0].isSet()) {
			o.xMin = origin.vectO.pos.getX();
			o.xMax = origin.vectO.pos.getX();
		} else if (origin.vect[0].pos.getX() < pos.getX()) {
			o.xMin = origin.vect[0].pos.getX();
			o.xMax = pos.getX();
		} else {
			o.xMin = pos.getX();
			o.xMax = origin.vect[0].pos.getX();
		}

		if (!origin.vect[1].isSet()) {
			o.yMin = origin.vectO.pos.getY();
			o.yMax = origin.vectO.pos.getY();
		} else if (origin.vect[1].pos.getY() < pos.getY()) {
			o.yMin = origin.vect[1].pos.getY();
			o.yMax = pos.getY();
		} else {
			o.yMin = pos.getY();
			o.yMax = origin.vect[1].pos.getY();
		}

		if (!origin.vect[2].isSet()) {
			o.zMin = origin.vectO.pos.getZ();
			o.zMax = origin.vectO.pos.getZ();
		} else if (origin.vect[2].pos.getZ() < pos.getZ()) {
			o.zMin = origin.vect[2].pos.getZ();
			o.zMax = pos.getZ();
		} else {
			o.zMin = pos.getZ();
			o.zMax = origin.vect[2].pos.getZ();
		}

		lasers = Utils.createLaserBox(worldObj, o.xMin, o.yMin, o.zMin, o.xMax, o.yMax, o.zMax, LaserKind.Red);
	}

	@Override
	public int xMin() {
		if (origin.isSet()) {
			return origin.xMin;
		}
		return pos.getX();
	}

	@Override
	public int yMin() {
		if (origin.isSet()) {
			return origin.yMin;
		}
		return pos.getY();
	}

	@Override
	public int zMin() {
		if (origin.isSet()) {
			return origin.zMin;
		}
		return pos.getZ();
	}

	@Override
	public int xMax() {
		if (origin.isSet()) {
			return origin.xMax;
		}
		return pos.getX();
	}

	@Override
	public int yMax() {
		if (origin.isSet()) {
			return origin.yMax;
		}
		return pos.getY();
	}

	@Override
	public int zMax() {
		if (origin.isSet()) {
			return origin.zMax;
		}
		return pos.getZ();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		TileMarker markerOrigin = null;

		if (origin.isSet()) {
			markerOrigin = origin.vectO.getMarker(worldObj);

			Origin o = origin;

			if (markerOrigin != null && markerOrigin.lasers != null) {
				for (EntityBlock entity : markerOrigin.lasers) {
					if (entity != null) {
						entity.setDead();
					}
				}
				markerOrigin.lasers = null;
			}

			for (TileWrapper m : o.vect) {
				TileMarker mark = m.getMarker(worldObj);

				if (mark != null) {
					if (mark.lasers != null) {
						for (EntityBlock entity : mark.lasers) {
							if (entity != null) {
								entity.setDead();
							}
						}
						mark.lasers = null;
					}

					if (mark != this) {
						mark.origin = new Origin();
					}
				}
			}

			if (markerOrigin != this && markerOrigin != null) {
				markerOrigin.origin = new Origin();
			}

			for (TileWrapper wrapper : o.vect) {
				TileMarker mark = wrapper.getMarker(worldObj);

				if (mark != null) {
					mark.updateSignals();
				}
			}
			if (markerOrigin != null) {
				markerOrigin.updateSignals();
			}
		}

		if (signals != null) {
			for (EntityBlock block : signals) {
				if (block != null) {
					block.setDead();
				}
			}
		}

		signals = null;

		if (!worldObj.isRemote && markerOrigin != null && markerOrigin != this) {
			markerOrigin.sendNetworkUpdate();
		}
	}

	@Override
	public void removeFromWorld() {
		if (!origin.isSet()) {
			return;
		}

		Origin o = origin;

		for (TileWrapper m : o.vect.clone()) {
			if (m.isSet()) {
				worldObj.setBlockToAir(m.pos);

				BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj, m.pos, BuildCraftBuilders.markerBlock.getDefaultState(), 0);
			}
		}

		worldObj.setBlockToAir(o.vectO.pos);

		BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj, o.vectO.pos, BuildCraftBuilders.markerBlock.getDefaultState(), 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("vectO")) {
			initVectO = new Position(nbttagcompound.getCompoundTag("vectO"));
			initVect = new Position[3];

			for (int i = 0; i < 3; ++i) {
				if (nbttagcompound.hasKey("vect" + i)) {
					initVect[i] = new Position(nbttagcompound.getCompoundTag("vect" + i));
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (origin.isSet() && origin.vectO.getMarker(worldObj) == this) {
			NBTTagCompound vectO = new NBTTagCompound();

			new Position(origin.vectO.getMarker(worldObj)).writeToNBT(vectO);
			nbttagcompound.setTag("vectO", vectO);

			for (int i = 0; i < 3; ++i) {
				if (origin.vect[i].isSet()) {
					NBTTagCompound vect = new NBTTagCompound();
					new Position(origin.vect[i].pos).writeToNBT(vect);
					nbttagcompound.setTag("vect" + i, vect);
				}
			}

		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		origin.writeData(stream);
		stream.writeBoolean(showSignals);
	}

	@Override
	public void readData(ByteBuf stream) {
		origin.readData(stream);
		showSignals = stream.readBoolean();

		switchSignals();

		if (origin.vectO.isSet() && origin.vectO.getMarker(worldObj) != null) {
			origin.vectO.getMarker(worldObj).updateSignals();

			for (TileWrapper w : origin.vect) {
				TileMarker m = w.getMarker(worldObj);

				if (m != null) {
					m.updateSignals();
				}
			}
		}

		createLasers();
	}

}
