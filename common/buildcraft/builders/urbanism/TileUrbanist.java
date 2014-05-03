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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import buildcraft.api.core.NetworkData;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IBoxesProvider;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.IRobotTask;
import buildcraft.core.robots.IRobotTaskProvider;
import buildcraft.core.robots.RobotTaskProviderRegistry;

public class TileUrbanist extends TileBuildCraft implements IInventory, IRobotTaskProvider, IBoxesProvider {

	public EntityUrbanist urbanist;

	@NetworkData
	public ArrayList<AnchoredBox> frames = new ArrayList<AnchoredBox>();

	private EntityLivingBase player;
	private int thirdPersonView = 0;
	private double posX, posY, posZ;
	private float yaw;
	private int p2x = 0;
	private int p2y = 0;
	private int p2z = 0;
	private boolean isCreatingFrame = false;
	private LinkedList<IRobotTask> tasks = new LinkedList<IRobotTask>();

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
		isCreatingFrame = true;
		AnchoredBox a = new AnchoredBox();
		a.box = new Box (x, y, z, x, y + 2, z);
		a.x1 = x;
		a.y1 = y;
		a.z1 = z;
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
		if (isCreatingFrame) {
			if (frames.size() > 0) {
				frames.get(frames.size() - 1).setP2(x, y, z);
			}
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
				b.box.kind = Kind.values()[kind];
			}
		}
	}

	@RPC (RPCSide.SERVER)
	public void startFiller (String fillerTag, Box box) {
		// TODO: This need to be updated to the new blueprint system
		/*BptBuilderBase builder = FillerPattern.patterns.get(fillerTag).getBlueprint(box, worldObj);

		List <SchematicBuilder> schematics = builder.getBuilders();

		FrameTask task = new FrameTask();
		task.frame = frames.get(frames.size() - 1);
		task.frame.box.kind = Kind.STRIPES;
		RPCHandler.rpcBroadcastPlayers(this, "setFrameKind", frames.size() - 1,
				Kind.STRIPES.ordinal());

		isCreatingFrame = false;

		for (SchematicBuilder b : schematics) {
			if (!b.isComplete()) {
				tasks.add(new TaskBuildSchematic(b, task));
				task.nbOfTasks++;
			}
		}*/
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
		Box box = new Box(this);

		for (AnchoredBox b : frames) {
			box.extendToEncompass(b.box);
		}

		return box.getBoundingBox();
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

	@Override
	public double getX() {
		return xCoord;
	}

	@Override
	public double getY() {
		return yCoord;
	}

	@Override
	public double getZ() {
		return zCoord;
	}

	@Override
	public boolean isActive() {
		return !isInvalid();
	}

	@Override
	public IRobotTask getNextTask(EntityRobot robot) {
		if (tasks.size() > 0) {
			return tasks.getFirst();
		} else {
			return null;
		}
	}

	@Override
	public void popNextTask() {
		tasks.removeFirst();
	}


	@Override
	public void initialize () {
		if (!worldObj.isRemote) {
			RobotTaskProviderRegistry.registerProvider(this);
		}
	}

	@Override
	public ArrayList<Box> getBoxes() {
		ArrayList<Box> result = new ArrayList<Box>();

		for (AnchoredBox b : frames) {
			result.add(b.box);
		}

		return result;
	}
}
