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

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IBoxesProvider;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.Utils;

public class TileUrbanist extends TileBuildCraft implements IInventory, IBoxesProvider, ICommandReceiver {

	public EntityUrbanist urbanist;

	public ArrayList<AnchoredBox> frames = new ArrayList<AnchoredBox>();

	private EntityLivingBase player;
	private int thirdPersonView = 0;
	private double posX, posY, posZ;
	private float yaw;
	private int p2x = 0;
	private int p2y = 0;
	private int p2z = 0;
	private boolean isCreatingFrame = false;

	// private LinkedList<IRobotTask> tasks = new LinkedList<IRobotTask>();

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

	private BuildCraftPacket createXYZPacket(String name, final int x, final int y, final int z) {
		return new PacketCommand(this, name, new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeInt(x);
				data.writeShort(y);
				data.writeInt(z);
			}
		});
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		// Non-XYZ commands go here
		if (side.isClient() && "setFrameKind".equals(command)) {
			setFrameKind(stream.readInt(), stream.readInt());
		} else if (side.isServer() && "startFiller".equals(command)) {
			String fillerTag = Utils.readUTF(stream);
			Box box = new Box();
			box.readData(stream);

			startFiller(fillerTag, box);
		} else {
			// XYZ commands go here
			int x = stream.readInt();
			int y = stream.readInt();
			int z = stream.readInt();

			if (side.isServer() && "setBlock".equals(command)) {
				worldObj.setBlock(x, y, z, Blocks.brick_block);
			} else if (side.isServer() && "eraseBlock".equals(command)) {
				// tasks.add(new UrbanistTaskErase(this, x, y, z));
			} else if ("createFrame".equals(command)) {
				createFrame(x, y, z);
			} else if ("moveFrame".equals(command)) {
				moveFrame(x, y, z);
			}
		}
	}

	public void rpcEraseBlock (int x, int y, int z) {
		BuildCraftCore.instance.sendToServer(createXYZPacket("eraseBlock", x, y, z));
	}

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
		BuildCraftCore.instance.sendToServer(createXYZPacket("createFrame", x, y, z));
	}

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
			BuildCraftCore.instance.sendToServer(createXYZPacket("moveFrame", x, y, z));
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

	public void setFrameKind (int id, int kind) {
		if (id < frames.size()) {
			AnchoredBox b = frames.get(id);

			if (b != null) {
				b.box.kind = Kind.values()[kind];
			}
		}
	}
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

	public void rpcStartFiller (final String fillerTag, final Box box) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "startFiller", new CommandWriter() {
			public void write(ByteBuf data) {
				Utils.writeUTF(data, fillerTag);
				box.writeData(data);
			}
		}));
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
	public void initialize () {

	}

	@Override
	public void writeData(ByteBuf stream) {
		stream.writeShort(frames.size());
		for (AnchoredBox b : frames) {
			b.writeData(stream);
		}
	}

	@Override
	public void readData(ByteBuf stream) {
		frames.clear();

		int size = stream.readUnsignedShort();
		for (int i = 0; i < size; i++) {
			AnchoredBox b = new AnchoredBox();
			b.readData(stream);
			frames.add(b);
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
