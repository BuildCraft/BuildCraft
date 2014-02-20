/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import buildcraft.builders.blueprints.BlueprintBuilder;
import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;
import buildcraft.builders.filler.pattern.FillerPattern;
import buildcraft.builders.urbanism.AnchoredBox.Kind;
import buildcraft.core.Box;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

public class TileUrbanist extends TileBuildCraft implements IInventory {

	public EntityUrbanist urbanist;
	EntityLivingBase player;
	int thirdPersonView = 0;

	double posX, posY, posZ;
	float yaw;

	int p2x = 0, p2y = 0, p2z = 0;

	@NetworkData
	public ArrayList <AnchoredBox> frames = new ArrayList <AnchoredBox> ();

	LinkedList <UrbanistTask> tasks = new LinkedList <UrbanistTask> ();

	public void createUrbanistEntity() {
		if (worldObj.isRemote) {
			if (urbanist == null) {
				urbanist = new EntityUrbanist(worldObj);
				worldObj.spawnEntityInWorld(urbanist);
				player = Minecraft.getMinecraft().renderViewEntity;

				urbanist.copyLocationAndAnglesFrom(player);
				urbanist.tile = this;
				urbanist.player = player;

				urbanist.rotationYaw = 0;
				urbanist.rotationPitch = 0;

				Minecraft.getMinecraft().renderViewEntity = urbanist;
				thirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
				Minecraft.getMinecraft().gameSettings.thirdPersonView = 8;

				posX = urbanist.posX;
				posY = urbanist.posY + 10;
				posZ = urbanist.posZ;

				yaw = 0;

				urbanist.setPositionAndRotation(posX, posY, posZ, yaw, 50);
				urbanist.setPositionAndUpdate(posX, posY, posZ);
			}
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
	}

	@RPC (RPCSide.SERVER)
	public void setBlock (int x, int y, int z) {
		worldObj.setBlock(x, y, z, Blocks.brick_block);
	}

	@RPC (RPCSide.SERVER)
	public void eraseBlock (int x, int y, int z) {
		tasks.add(new UrbanistTaskErase(this, x, y, z));
	}

	public void rpcEraseBlock (int x, int y, int z) {
		RPCHandler.rpcServer(this, "eraseBlock", x, y, z);
	}

	@RPC (RPCSide.BOTH)
	public void createFrame (int x, int y, int z) {
		AnchoredBox a = new AnchoredBox();
		a.box = new Box (x + 0.5F, y + 0.5F, z + 0.5F, x + 0.5F, y + 2.5F, z + 0.5F);
		a.x1 = x + 0.5F;
		a.y1 = y + 0.5F;
		a.z1 = z + 0.5F;
		frames.add(a);
	}

	public void rpcCreateFrame (int x, int y, int z) {
		p2x = x;
		p2y = y;
		p2z = z;

		// TODO: this is OK in SMP, but the frame actually needs to be
		// broadcasted to all players
		createFrame(x, y, z);
		RPCHandler.rpcServer(this, "createFrame", x, y, z);
	}

	@RPC (RPCSide.BOTH)
	public void moveFrame (int x, int y, int z) {
		if (frames.size() > 0) {
			frames.get(frames.size() - 1).setP2(x + 0.5F, y + 0.5F, z + 0.5F);
		}
	}

	public void rpcMoveFrame (int x, int y, int z) {
		if (p2x != x || p2y != y || p2z != z) {
			p2x = x;
			p2y = y;
			p2z = z;

			// TODO: this is OK in SMP, but the frame actually needs to be
			// broadcasted to all players
			moveFrame(x, y, z);
			RPCHandler.rpcServer(this, "moveFrame", x, y, z);
		}
	}

	public class FrameTask {
		int nbOfTasks;
		AnchoredBox frame;

		public void taskDone () {
			nbOfTasks--;

			if (nbOfTasks <= 0) {
				frames.remove(frame);
			}
		}
	}

	@RPC (RPCSide.CLIENT)
	public void setFrameKind (int id, int kind) {
		if (id < frames.size()) {
			AnchoredBox b = frames.get(id);

			if (b != null) {
				System.out.println ("SWITCH " + id + " TO " + kind);
				b.kind = Kind.values()[kind];
			}
		}
	}

	@RPC (RPCSide.SERVER)
	public void startFiller (String fillerTag, Box box) {
		BlueprintBuilder builder = FillerPattern.patterns.get(fillerTag).getBlueprint(box, worldObj);

		List <SchematicBuilder> schematics = builder.getBuilders();

		/*if (frame != null) {
			frame.setDead();
			frame = null;
		}

		EntityFrame newFrame = new EntityFrame(worldObj, box);
		newFrame.setKind(Kind.STRIPES);
		worldObj.spawnEntityInWorld(newFrame);
*/

		FrameTask task = new FrameTask();
		task.frame = frames.get(frames.size() - 1);
		task.frame.kind = Kind.STRIPES;
		RPCHandler.rpcBroadcastPlayers(this, "setFrameKind", frames.size() - 1,
				Kind.STRIPES.ordinal());

		for (SchematicBuilder b : schematics) {
			if (!b.isComplete()) {
				tasks.add(new UrbanistTaskBuildSchematic(this, b, task));
				task.nbOfTasks++;
			}
		}
	}

	public void rpcStartFiller (String fillerTag, Box box) {
		RPCHandler.rpcServer(this, "startFiller", fillerTag, box);
	}

	public void destroyUrbanistEntity() {
		Minecraft.getMinecraft().renderViewEntity = player;
		Minecraft.getMinecraft().gameSettings.thirdPersonView = thirdPersonView;
		worldObj.removeEntity(urbanist);
		urbanist.setDead();
		urbanist = null;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		double xMin = xCoord;
		double yMin = yCoord;
		double zMin = zCoord;
		double xMax = xCoord + 1.0;
		double yMax = yCoord + 1.0;
		double zMax = zCoord + 1.0;

		for (AnchoredBox b : frames) {
			if (b.box.xMin < xMin) {
				xMin = b.box.xMin;
			}

			if (b.box.yMin < yMin) {
				yMin = b.box.yMin;
			}

			if (b.box.zMin < zMin) {
				zMin = b.box.zMin;
			}

			if (b.box.xMax > xMax) {
				xMax = b.box.xMax;
			}

			if (b.box.yMax > yMax) {
				yMax = b.box.yMax;
			}

			if (b.box.zMax > zMax) {
				zMax = b.box.zMax;
			}
		}

		return AxisAlignedBB.getBoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("nbFrames", frames.size());

		for (int i = 0; i < frames.size(); ++i) {
			NBTTagCompound cpt = new NBTTagCompound();
			frames.get(i).writeToNBT(cpt);
			nbt.setTag("frame[" + i + "]", cpt);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		frames.clear();

		int size = nbt.getInteger("nbFrames");

		for (int i = 0; i < size; ++i) {
			AnchoredBox b = new AnchoredBox();
			b.readFromNBT(nbt.getCompoundTag("frame[" + i + "]"));
			frames.add(b);
		}
	}
}
