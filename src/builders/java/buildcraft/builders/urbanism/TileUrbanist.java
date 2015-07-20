/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.internal.IBoxesProvider;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;

import io.netty.buffer.ByteBuf;

public class TileUrbanist extends TileBuildCraft implements IInventory, IBoxesProvider, ICommandReceiver {

    public EntityUrbanist urbanist;

    public ArrayList<AnchoredBox> frames = new ArrayList<AnchoredBox>();

    private Entity player;
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
                player = Minecraft.getMinecraft().getRenderViewEntity();

                urbanist.copyLocationAndAnglesFrom(player);
                urbanist.tile = this;
                urbanist.player = player;

                urbanist.rotationYaw = 0;
                urbanist.rotationPitch = 0;

                Minecraft.getMinecraft().setRenderViewEntity(urbanist);
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
    public void update() {
        super.update();
    }

    private Packet createXYZPacket(String name, final BlockPos pos) {
        return new PacketCommand(this, name, new CommandWriter() {
            public void write(ByteBuf data) {
                data.writeInt(pos.getX());
                data.writeShort(pos.getY());
                data.writeInt(pos.getZ());
            }
        });
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        // Non-XYZ commands go here
        if (side.isClient() && "setFrameKind".equals(command)) {
            setFrameKind(stream.readInt(), stream.readInt());
        } else if (side.isServer() && "startFiller".equals(command)) {
            String fillerTag = NetworkUtils.readUTF(stream);
            Box box = new Box();
            box.readData(stream);

            startFiller(fillerTag, box);
        } else {
            // XYZ commands go here
            int x = stream.readInt();
            int y = stream.readInt();
            int z = stream.readInt();
            BlockPos pos = new BlockPos(x, y, z);

            if (side.isServer() && "setBlock".equals(command)) {
                worldObj.setBlockState(pos, Blocks.brick_block.getDefaultState());
            } else if (side.isServer() && "eraseBlock".equals(command)) {
                // tasks.add(new UrbanistTaskErase(this, pos));
            } else if ("createFrame".equals(command)) {
                createFrame(pos);
            } else if ("moveFrame".equals(command)) {
                moveFrame(pos);
            }
        }
    }

    public void rpcEraseBlock(BlockPos pos) {
        BuildCraftCore.instance.sendToServer(createXYZPacket("eraseBlock", pos));
    }

    public void createFrame(BlockPos pos) {
        isCreatingFrame = true;
        AnchoredBox a = new AnchoredBox();
        a.box = new Box(pos, pos.up(2));
        a.x1 = pos.getX();
        a.y1 = pos.getY();
        a.z1 = pos.getZ();
        frames.add(a);
    }

    public void rpcCreateFrame(BlockPos pos) {
        p2x = pos.getX();
        p2y = pos.getY();
        p2z = pos.getZ();

        // TODO: this is OK in SMP, but the frame actually needs to be
        // broadcasted to all players
        createFrame(pos);
        BuildCraftCore.instance.sendToServer(createXYZPacket("createFrame", pos));
    }

    public void moveFrame(BlockPos pos) {
        if (isCreatingFrame) {
            if (frames.size() > 0) {
                frames.get(frames.size() - 1).setP2(pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    public void rpcMoveFrame(BlockPos pos) {
        if (p2x != pos.getX() || p2y != pos.getY() || p2z != pos.getZ()) {
            p2x = pos.getX();
            p2y = pos.getY();
            p2z = pos.getZ();

            // TODO: this is OK in SMP, but the frame actually needs to be
            // broadcasted to all players
            moveFrame(pos);
            BuildCraftCore.instance.sendToServer(createXYZPacket("moveFrame", pos));
        }
    }

    public class FrameTask {
        int nbOfTasks;
        AnchoredBox frame;

        public void taskDone() {
            nbOfTasks--;

            if (nbOfTasks <= 0) {
                frames.remove(frame);
            }
        }
    }

    public void setFrameKind(int id, int kind) {
        if (id < frames.size()) {
            AnchoredBox b = frames.get(id);

            if (b != null) {
                b.box.kind = Kind.values()[kind];
            }
        }
    }

    public void startFiller(String fillerTag, Box box) {
        // TODO: This need to be updated to the new blueprint system
        /* BptBuilderBase builder = FillerPattern.patterns.get(fillerTag).getBlueprint(box, worldObj); List
         * <SchematicBuilder> schematics = builder.getBuilders(); FrameTask task = new FrameTask(); task.frame =
         * frames.get(frames.size() - 1); task.frame.box.kind = Kind.STRIPES; RPCHandler.rpcBroadcastPlayers(this,
         * "setFrameKind", frames.size() - 1, Kind.STRIPES.ordinal()); isCreatingFrame = false; for (SchematicBuilder b
         * : schematics) { if (!b.isComplete()) { tasks.add(new TaskBuildSchematic(b, task)); task.nbOfTasks++; } } */
    }

    public void rpcStartFiller(final String fillerTag, final Box box) {
        BuildCraftCore.instance.sendToServer(new PacketCommand(this, "startFiller", new CommandWriter() {
            public void write(ByteBuf data) {
                NetworkUtils.writeUTF(data, fillerTag);
                box.writeData(data);
            }
        }));
    }

    public void destroyUrbanistEntity() {
        Minecraft.getMinecraft().setRenderViewEntity(player);
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
    public void setInventorySlotContents(int var1, ItemStack var2) {}

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
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
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

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
    public void initialize() {

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
