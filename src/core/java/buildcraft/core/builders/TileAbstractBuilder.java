/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders;

import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.ITileBuilder;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.LaserData;
import buildcraft.core.internal.IBoxProvider;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;

public abstract class TileAbstractBuilder extends TileBuildCraft implements ITileBuilder, IInventory, IBoxProvider, IBuildingItemsProvider,
        ICommandReceiver {

    public LinkedList<LaserData> pathLasers = new LinkedList<LaserData>();

    public Set<BuildingItem> buildersInAction = Sets.newConcurrentHashSet();

    private int rfPrev = 0;
    private int rfUnchangedCycles = 0;

    public TileAbstractBuilder() {
        super();
        /** The builder should not act as a gigantic energy buffer, thus we keep enough build energy to build about 2
         * stacks' worth of blocks. */
        this.setBattery(new RFBattery(2 * 64 * BuilderAPI.BUILD_ENERGY, 1000, 0));
    }

    @Override
    public void initialize() {
        super.initialize();

        if (worldObj.isRemote) {
            BuildCraftCore.instance.sendToServer(new PacketCommand(this, "uploadBuildersInAction", null));
        }
    }

    private Packet createLaunchItemPacket(final BuildingItem i) {
        return new PacketCommand(this, "launchItem", new CommandWriter() {
            public void write(ByteBuf data) {
                i.writeData(data);
            }
        });
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        if (side.isServer() && "uploadBuildersInAction".equals(command)) {
            for (BuildingItem i : buildersInAction) {
                BuildCraftCore.instance.sendToPlayer((EntityPlayer) sender, createLaunchItemPacket(i));
            }
        } else if (side.isClient() && "launchItem".equals(command)) {
            BuildingItem item = new BuildingItem();
            item.readData(stream);
            buildersInAction.add(item);
        }
    }

    @Override
    public void update() {
        super.update();

        RFBattery battery = this.getBattery();

        if (rfPrev != battery.getEnergyStored()) {
            rfPrev = battery.getEnergyStored();
            rfUnchangedCycles = 0;
        }

        Iterator<BuildingItem> itemIterator = buildersInAction.iterator();
        BuildingItem i;

        while (itemIterator.hasNext()) {
            i = itemIterator.next();
            i.update();

            if (i.isDone()) {
                itemIterator.remove();
            }
        }

        if (rfPrev != battery.getEnergyStored()) {
            rfPrev = battery.getEnergyStored();
            rfUnchangedCycles = 0;
        }

        rfUnchangedCycles++;

        /** After 100 cycles with no consumption or additional power, start to slowly to decrease the amount of power
         * available in the builder. */
        if (rfUnchangedCycles > 100) {
            battery.useEnergy(0, 1000, false);

            rfPrev = battery.getEnergyStored();
        }
    }

    @Override
    public Collection<BuildingItem> getBuilders() {
        return buildersInAction;
    }

    public LinkedList<LaserData> getPathLaser() {
        return pathLasers;
    }

    @Override
    public void addAndLaunchBuildingItem(BuildingItem item) {
        buildersInAction.add(item);
        BuildCraftCore.instance.sendToPlayersNear(createLaunchItemPacket(item), this);
    }

    public final int energyAvailable() {
        return getBattery().getEnergyStored();
    }

    public final boolean consumeEnergy(int quantity) {
        return getBattery().useEnergy(quantity, quantity, false) > 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        rfPrev = getBattery().getEnergyStored();
        rfUnchangedCycles = 0;
    }

    @Override
    public void readData(ByteBuf stream) {
        super.readData(stream);
        int size = stream.readUnsignedShort();
        pathLasers.clear();
        for (int i = 0; i < size; i++) {
            LaserData ld = new LaserData();
            ld.readData(stream);
            pathLasers.add(ld);
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        super.writeData(stream);
        stream.writeShort(pathLasers.size());
        for (LaserData ld : pathLasers) {
            ld.writeData(stream);
        }
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    public boolean drainBuild(FluidStack fluidStack, boolean realDrain) {
        return false;
    }
}
