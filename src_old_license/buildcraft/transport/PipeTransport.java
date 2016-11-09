/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.core.lib.utils.BlockUtils;

public abstract class PipeTransport {
    public TileGenericPipe container;
    protected boolean shouldSendPacket = false;

    protected boolean[] inputsOpen = new boolean[EnumFacing.VALUES.length];
    protected boolean[] outputsOpen = new boolean[EnumFacing.VALUES.length];

    public PipeTransport() {
        for (int b = 0; b < EnumFacing.VALUES.length; b++) {
            inputsOpen[b] = true;
            outputsOpen[b] = true;
        }
    }

    public abstract IPipeTile.PipeType getPipeType();

    public World getWorld() {
        return container.getWorld();
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("inputOpen") && nbt.hasKey("outputOpen")) {
            BitSet inputBuf = BitSet.valueOf(new byte[] { nbt.getByte("inputOpen") });
            BitSet outputBuf = BitSet.valueOf(new byte[] { nbt.getByte("outputOpen") });

            for (int b = 0; b < EnumFacing.VALUES.length; b++) {
                inputsOpen[b] = inputBuf.get(b);
                outputsOpen[b] = outputBuf.get(b);
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        BitSet inputBuf = new BitSet(EnumFacing.VALUES.length);
        BitSet outputBuf = new BitSet(EnumFacing.VALUES.length);

        for (int b = 0; b < EnumFacing.VALUES.length; b++) {
            if (inputsOpen[b]) {
                inputBuf.set(b, true);
            } else {
                inputBuf.set(b, false);
            }

            if (outputsOpen[b]) {
                outputBuf.set(b, true);
            } else {
                outputBuf.set(b, false);
            }
        }

        nbt.setByte("inputOpen", inputBuf.toByteArray()[0]);
        nbt.setByte("outputOpen", outputBuf.toByteArray()[0]);
    }

    public void updateEntity() {}

    public void setTile(TileGenericPipe tile) {
        this.container = tile;
    }

    public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
        return true;
    }

    public void onNeighborChange(EnumFacing direction) {}

    public void onBlockPlaced() {}

    public void initialize() {}

    public boolean inputOpen(EnumFacing from) {
        return inputsOpen[from.ordinal()];
    }

    public boolean outputOpen(EnumFacing to) {
        return outputsOpen[to.ordinal()];
    }

    public void allowInput(EnumFacing from, boolean allow) {
        if (from != null) {
            inputsOpen[from.ordinal()] = allow;
        }
    }

    public void allowOutput(EnumFacing to, boolean allow) {
        if (to != null) {
            outputsOpen[to.ordinal()] = allow;
        }
    }

    public void dropContents() {}

    public List<ItemStack> getDroppedItems() {
        return new ArrayList<>();
    }

    public void synchronizeNetwork(EntityPlayer player) {}

    public boolean delveIntoUnloadedChunks() {
        return false;
    }

    protected void destroyPipe() {
        BlockUtils.explodeBlock(container.getWorld(), container.getPos());
        container.getWorld().setBlockToAir(container.getPos());
    }
}
