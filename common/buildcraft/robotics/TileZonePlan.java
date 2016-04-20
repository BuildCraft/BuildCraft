/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import java.util.Arrays;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftRobotics;
import buildcraft.api.core.IZone;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.items.INamedItem;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.robotics.gui.ContainerZonePlan;
import buildcraft.robotics.map.MapWorld;

public class TileZonePlan extends TileBuildCraft implements IInventory {

    public static final int RESOLUTION = 2048;
    public static final int CRAFT_TIME = 120;
    private static final int PREVIEW_BLOCKS_PER_PIXEL = 10;
    private static int RESOLUTION_CHUNKS = RESOLUTION >> 4;

    public int chunkStartX, chunkStartZ;
    public short progress = 0;
    public String mapName = "";

    private final byte[] previewColors = new byte[80];
    private final SimpleInventory inv = new SimpleInventory(3, "inv", 64);
    private final SafeTimeTracker previewRecalcTimer = new SafeTimeTracker(100);

    private boolean previewColorsPushed = false;
    private ZonePlan[] selectedAreas = new ZonePlan[16];
    private int currentSelectedArea = 0;

    public byte[] getPreviewTexture(boolean force) {
        if (!previewColorsPushed || force) {
            previewColorsPushed = true;
            return previewColors;
        }
        return null;
    }

    @Override
    public void initialize() {
        super.initialize();

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        chunkStartX = chunkX - RESOLUTION_CHUNKS / 2;
        chunkStartZ = chunkZ - RESOLUTION_CHUNKS / 2;
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            return;
        }

        if (previewRecalcTimer.markTimeIfDelay(worldObj)) {
            recalculatePreview();
        }

        if (inv.getStackInSlot(0) != null && inv.getStackInSlot(1) == null && inv.getStackInSlot(0).getItem() instanceof ItemMapLocation) {

            if (progress < CRAFT_TIME) {
                progress++;

                if (worldObj.getTotalWorldTime() % 5 == 0) {
                    sendNetworkUpdate();
                }
            } else {
                ItemStack stack = inv.decrStackSize(0, 1);

                if (selectedAreas[currentSelectedArea] != null) {
                    ItemMapLocation.setZone(stack, selectedAreas[currentSelectedArea]);
                    ((INamedItem) stack.getItem()).setName(stack, mapName);
                }

                inv.setInventorySlotContents(1, stack);
            }
        } else if (progress != 0) {
            progress = 0;
            sendNetworkUpdate();
        }
    }

    private void recalculatePreview() {
        byte[] newPreviewColors = new byte[80];
        MapWorld mw = BuildCraftRobotics.manager.getWorld(worldObj);

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 10; x++) {
                int tx = (x * PREVIEW_BLOCKS_PER_PIXEL) - (5 * PREVIEW_BLOCKS_PER_PIXEL) + (PREVIEW_BLOCKS_PER_PIXEL / 2);
                int ty = (y * PREVIEW_BLOCKS_PER_PIXEL) - (4 * PREVIEW_BLOCKS_PER_PIXEL) + (PREVIEW_BLOCKS_PER_PIXEL / 2);
                newPreviewColors[y * 10 + x] = (byte) mw.getColor(getPos().getX() - (getPos().getX() % PREVIEW_BLOCKS_PER_PIXEL) + tx, getPos().getZ()
                    - (getPos().getZ() % PREVIEW_BLOCKS_PER_PIXEL) + ty);
            }
        }

        if (!Arrays.equals(previewColors, newPreviewColors)) {
            System.arraycopy(newPreviewColors, 0, previewColors, 0, 80);
            sendNetworkUpdate();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("name", mapName);

        NBTTagCompound invNBT = new NBTTagCompound();
        inv.writeToNBT(invNBT);
        nbt.setTag("inv", invNBT);

        for (int i = 0; i < selectedAreas.length; ++i) {
            if (selectedAreas[i] != null) {
                NBTTagCompound subNBT = new NBTTagCompound();
                selectedAreas[i].writeToNBT(subNBT);
                nbt.setTag("selectedArea[" + i + "]", subNBT);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        mapName = nbt.getString("name");

        if (mapName == null) {
            mapName = "";
        }

        inv.readFromNBT(nbt.getCompoundTag("inv"));

        for (int i = 0; i < selectedAreas.length; ++i) {
            if (nbt.hasKey("selectedArea[" + i + "]")) {
                selectedAreas[i] = new ZonePlan();
                selectedAreas[i].readFromNBT(nbt.getCompoundTag("selectedArea[" + i + "]"));
            }
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        stream.writeShort(progress);
        NetworkUtils.writeUTF(stream, mapName);
        stream.writeBytes(previewColors, 0, 80);
    }

    @Override
    public void readData(ByteBuf stream) {
        progress = stream.readShort();
        mapName = NetworkUtils.readUTF(stream);
        stream.readBytes(previewColors, 0, 80);
        previewColorsPushed = false;
    }

    private void importMap(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof IMapLocation) {
            final IZone zone = ((IMapLocation) stack.getItem()).getZone(stack);
            if (zone != null && zone instanceof ZonePlan) {
                selectedAreas[currentSelectedArea] = (ZonePlan) zone;

                for (EntityPlayerMP e : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    if (e.openContainer != null && e.openContainer instanceof ContainerZonePlan && ((ContainerZonePlan) e.openContainer)
                            .getTile() == this) {
                        Packet p = new PacketCommand(e.openContainer, "areaLoaded", new CommandWriter() {
                            @Override
                            public void write(ByteBuf data) {
                                ((ZonePlan) zone).writeData(data);
                            }
                        });

                        BuildCraftCore.instance.sendToPlayer(e, p);
                    }
                }
            }
        }
    }

    public ZonePlan selectArea(int index) {
        if (selectedAreas[index] == null) {
            selectedAreas[index] = new ZonePlan();
        }

        currentSelectedArea = index;

        return selectedAreas[index];
    }

    public void setArea(int index, ZonePlan area) {
        selectedAreas[index] = area;
    }

    @Override
    public int getSizeInventory() {
        return inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotId) {
        return inv.getStackInSlot(slotId);
    }

    @Override
    public ItemStack decrStackSize(int slotId, int count) {
        return inv.decrStackSize(slotId, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int slotId) {
        return inv.removeStackFromSlot(slotId);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        inv.setInventorySlotContents(slotId, itemstack);

        if (!worldObj.isRemote && slotId == 2) {
            importMap(itemstack);
        }
    }

    @Override
    public IChatComponent getDisplayName() {
        return inv.getDisplayName();
    }

    @Override
    public boolean hasCustomName() {
        return inv.hasCustomName();
    }

    @Override
    public int getInventoryStackLimit() {
        return inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return inv.isUseableByPlayer(entityplayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        inv.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        inv.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return inv.isItemValidForSlot(i, itemstack);
    }
}
