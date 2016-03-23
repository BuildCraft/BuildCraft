/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IEngine;
import buildcraft.api.power.IRedstoneEngine;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.CompatHooks;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.utils.AverageDouble;
import buildcraft.core.lib.utils.AverageInt;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerIron;
import buildcraft.transport.pipes.PipePowerEmerald;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerSandstone;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;

public class PipeTransportPower extends PipeTransport implements IDebuggable {
    public static final Map<Class<? extends Pipe<?>>, Integer> powerCapacities = new HashMap<>();
    public static final Map<Class<? extends Pipe<?>>, Float> powerResistances = new HashMap<>();
    private static int MAX_POWER = 0;

    private static final int OVERLOAD_TICKS = 60;
    public static final short POWER_STAGES = 1 << 6;

    public AverageDouble[] displayPowerAverage = new AverageDouble[6];

    public short[] displayPower = new short[6];
    public short[] displayFlow = new short[6];
    public int[] nextPowerQuery = new int[6];
    public double[] internalNextPower = new double[6];
    public int overload;
    public int maxPower = 80;
    public float powerResistance;

    public int[] dbgEnergyInput = new int[6];
    public int[] dbgEnergyOutput = new int[6];
    public int[] dbgEnergyOffered = new int[6];

    private final AverageInt[] powerAverage = new AverageInt[6];
    private final TileEntity[] tiles = new TileEntity[6];
    private final Object[] providers = new Object[6];

    private boolean needsInit = true;

    private int[] powerQuery = new int[6];

    private long currentDate;
    private double[] internalPower = new double[6];

    private SafeTimeTracker tracker = new SafeTimeTracker(2 * BuildCraftCore.updateFactor);

    /** Used at the client to show flow properly */
    public double[] clientDisplayFlow = new double[6];
    public Vec3 clientDisplayFlowCentre = Utils.VEC_ZERO;
    public long clientLastDisplayTime = 0;

    public PipeTransportPower() {
        for (int i = 0; i < 6; ++i) {
            powerQuery[i] = 0;
            powerAverage[i] = new AverageInt(10);
            displayPowerAverage[i] = new AverageDouble(10);
        }
    }

    @Override
    public IPipeTile.PipeType getPipeType() {
        return IPipeTile.PipeType.POWER;
    }

    public void initFromPipe(Class<? extends Pipe<?>> pipeClass) {
        if (BuildCraftTransport.usePipeLoss) {
            maxPower = 10240;
            powerResistance = powerResistances.get(pipeClass);
        } else {
            maxPower = powerCapacities.get(pipeClass);
        }
    }

    @Override
    public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
        if (tile instanceof IPipeTile) {
            Pipe<?> pipe2 = (Pipe<?>) ((IPipeTile) tile).getPipe();
            if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportPower)) {
                return false;
            }
            return true;
        }

        if (container.pipe instanceof PipePowerWood) {
            return isPowerSource(tile, side);
        } else {
            if (tile instanceof IEngine) {
                // Disregard engines for this.
                return false;
            }

            Object provider = CompatHooks.INSTANCE.getEnergyProvider(tile);

            if (provider instanceof IEnergyHandler || provider instanceof IEnergyReceiver) {
                IEnergyConnection handler = (IEnergyConnection) provider;
                if (handler.canConnectEnergy(side.getOpposite())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isPowerSource(TileEntity tile, EnumFacing side) {
        if (tile instanceof TileBuildCraft && !(tile instanceof IEngine)) {
            // Disregard non-engine BC tiles.
            // While this, of course, does nothing to work with other mods,
            // it at least makes it work nicely with BC's built-in blocks while
            // the new RF api isn't out.
            return false;
        }

        if (tile instanceof IRedstoneEngine) {
            // Do not render wooden pipe connections to match the look of transport/fluid pipes
            // for kinesis.
            return false;
        }

        return tile instanceof IEnergyConnection && ((IEnergyConnection) tile).canConnectEnergy(side.getOpposite());
    }

    @Override
    public void onNeighborChange(EnumFacing side) {
        super.onNeighborChange(side);
        updateTile(side);
    }

    private void updateTile(EnumFacing side) {
        int o = side.ordinal();
        TileEntity tile = container.getTile(side);
        if (tile != null && container.isPipeConnected(side)) {
            tiles[o] = tile;
        } else {
            tiles[o] = null;
            internalPower[o] = 0;
            internalNextPower[o] = 0;
            powerAverage[o].clear();
            displayFlow[o] = 0;
        }
        providers[o] = getEnergyProvider(o);
    }

    private void init() {
        if (needsInit) {
            needsInit = false;
            for (EnumFacing side : EnumFacing.VALUES) {
                updateTile(side);
            }
        }
    }

    private Object getEnergyProvider(int side) {
        EnumFacing fs = EnumFacing.getFront(side);
        if (container.hasPipePluggable(fs)) {
            Object pp = container.getPipePluggable(fs);
            if (pp instanceof IEnergyReceiver) {
                return pp;
            }
        }
        return CompatHooks.INSTANCE.getEnergyProvider(tiles[side]);
    }

    @Override
    public void updateEntity() {
        if (container.getWorld().isRemote) {
            for (int i = 0; i < 6; i++) {
                displayPowerAverage[i].tick(displayPower[i]);
            }
            return;
        }

        step();

        init();

        for (EnumFacing side : EnumFacing.VALUES) {
            if (tiles[side.ordinal()] != null && tiles[side.ordinal()].isInvalid()) {
                updateTile(side);
            }
        }

        // FIXME: LEFT OVER FROM MERGE! LOOK AT THIS!
        Arrays.fill(displayFlow, (short) 0);

        // Send the power to nearby pipes who requested it
        for (int i = 0; i < 6; ++i) {
            if (internalPower[i] > 0) {
                int totalPowerQuery = 0;
                for (int j = 0; j < 6; ++j) {
                    if (j != i && powerQuery[j] > 0) {
                        Object ep = providers[j];
                        if (ep instanceof IPipeTile || ep instanceof IEnergyReceiver || ep instanceof IEnergyHandler) {
                            totalPowerQuery += powerQuery[j];
                        }
                    }
                }

                if (totalPowerQuery > 0) {
                    int unusedPowerQuery = totalPowerQuery;
                    for (int j = 0; j < 6; ++j) {
                        if (j != i && powerQuery[j] > 0) {
                            Object ep = providers[j];
                            double watts = Math.min(internalPower[i] * powerQuery[j] / unusedPowerQuery, internalPower[i]);
                            unusedPowerQuery -= powerQuery[j];

                            if (ep instanceof IPipeTile && ((IPipeTile) ep).getPipeType() == IPipeTile.PipeType.POWER) {
                                Pipe<?> nearbyPipe = (Pipe<?>) ((IPipeTile) ep).getPipe();
                                PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyPipe.transport;
                                watts = nearbyTransport.receiveEnergy(EnumFacing.VALUES[j].getOpposite(), watts);
                                internalPower[i] -= watts;
                                dbgEnergyOutput[j] += watts;

                                powerAverage[j].push((int) Math.ceil(watts));
                                powerAverage[i].push((int) Math.ceil(watts));

                                displayFlow[i] = 1;
                                displayFlow[j] = -1;
                            } else {
                                int iWatts = (int) watts;
                                if (ep instanceof IEnergyReceiver) {
                                    IEnergyReceiver handler = (IEnergyReceiver) ep;
                                    if (handler.canConnectEnergy(EnumFacing.values()[j].getOpposite())) {
                                        iWatts = handler.receiveEnergy(EnumFacing.values()[j].getOpposite(), iWatts, false);
                                    }
                                }

                                internalPower[i] -= iWatts;
                                dbgEnergyOutput[j] += iWatts;

                                powerAverage[j].push(iWatts);
                                powerAverage[i].push(iWatts);

                                displayFlow[i] = 1;
                                displayFlow[j] = -1;
                            }

                        }
                    }
                }
            }
        }

        short highestPower = 0;
        for (int i = 0; i < 6; i++) {
            powerAverage[i].tick();
            displayPower[i] = (short) Math.round(powerAverage[i].getAverage());
            if (displayPower[i] > highestPower) {
                highestPower = displayPower[i];
            }
        }

        overload += highestPower > (maxPower * 0.95F) ? 1 : -1;
        if (overload < 0) {
            overload = 0;
        }
        if (overload > OVERLOAD_TICKS) {
            overload = OVERLOAD_TICKS;
        }

        // Compute the tiles requesting energy that are not power pipes
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (!outputOpen(dir)) {
                continue;
            }

            Object tile = providers[dir.ordinal()];

            if (tile instanceof IPipeTile && ((IPipeTile) tile).getPipe() != null && ((Pipe<?>) ((IPipeTile) tile)
                    .getPipe()).transport instanceof PipeTransportPower) {
                continue;
            }
            if (tile instanceof IEnergyReceiver) {
                IEnergyReceiver handler = (IEnergyReceiver) tile;
                if (handler.canConnectEnergy(dir.getOpposite())) {
                    int request = handler.receiveEnergy(dir.getOpposite(), this.maxPower, true);
                    if (request > 0) {
                        requestEnergy(dir, request);
                    }
                }
            } else if (tile instanceof IEnergyReceiver) {
                IEnergyReceiver handler = (IEnergyReceiver) tile;
                if (handler.canConnectEnergy(dir.getOpposite())) {
                    int request = handler.receiveEnergy(dir.getOpposite(), this.maxPower, true);
                    if (request > 0) {
                        requestEnergy(dir, request);
                    }
                }
            }
        }

        // Sum the amount of energy requested on each side
        int[] transferQuery = new int[6];
        for (int i = 0; i < 6; ++i) {
            transferQuery[i] = 0;
            if (!inputOpen(EnumFacing.getFront(i))) {
                continue;
            }
            for (int j = 0; j < 6; ++j) {
                if (j != i) {
                    transferQuery[i] += powerQuery[j];
                }
            }
            transferQuery[i] = Math.min(transferQuery[i], maxPower);
        }

        // Transfer the requested energy to nearby pipes
        for (int i = 0; i < 6; ++i) {
            if (transferQuery[i] != 0 && tiles[i] != null) {
                TileEntity entity = tiles[i];
                if (entity instanceof IPipeTile && ((IPipeTile) entity).getPipeType() == IPipeTile.PipeType.POWER) {
                    IPipeTile nearbyTile = (IPipeTile) entity;
                    if (nearbyTile.getPipe() == null || nearbyTile.getPipeType() != IPipeTile.PipeType.POWER) {
                        continue;
                    }
                    PipeTransportPower nearbyTransport = (PipeTransportPower) ((Pipe<?>) nearbyTile.getPipe()).transport;
                    nearbyTransport.requestEnergy(EnumFacing.VALUES[i].getOpposite(), transferQuery[i]);
                }
            }
        }

        if (tracker.markTimeIfDelay(container.getWorld())) {
            PacketPowerUpdate packet = new PacketPowerUpdate(container);
            packet.displayPower = new short[6];
            for (int i = 0; i < 6; i++) {
                double val = displayPower[i];
                val /= MAX_POWER;
                val = Math.sqrt(val);
                val *= POWER_STAGES;
                packet.displayPower[i] = (short) val;
            }
            packet.displayFlow = displayFlow;
            packet.overload = isOverloaded();
            BuildCraftTransport.instance.sendToPlayersNear(packet, container);
        }
    }

    public boolean isOverloaded() {
        return overload >= OVERLOAD_TICKS;
    }

    private void step() {
        if (container != null && container.getWorld() != null && currentDate != container.getWorld().getTotalWorldTime()) {
            currentDate = container.getWorld().getTotalWorldTime();

            Arrays.fill(dbgEnergyInput, 0);
            Arrays.fill(dbgEnergyOffered, 0);
            Arrays.fill(dbgEnergyOutput, 0);

            powerQuery = nextPowerQuery;
            nextPowerQuery = new int[6];

            double[] next = internalPower;
            internalPower = internalNextPower;
            internalNextPower = next;
        }
    }

    /** Do NOT ever call this from outside Buildcraft. It is NOT part of the API. All power input MUST go through
     * designated input pipes, such as Wooden Power Pipes or a subclass thereof.
     * 
     * Otherwise you will make us very sad :( */
    public double receiveEnergy(EnumFacing from, double tVal) {
        int side = from.ordinal();
        double val = tVal;

        step();

        dbgEnergyOffered[side] += val;

        if (this.container.pipe instanceof IPipeTransportPowerHook) {
            double ret = ((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, (int) val);
            if (ret >= 0) {
                return ret;
            }
        }

        if (internalNextPower[side] > maxPower) {
            return 0;
        }

        if (BuildCraftTransport.usePipeLoss) {
            internalNextPower[side] += val * (1.0F - powerResistance);
        } else {
            internalNextPower[side] += val;
        }

        if (internalNextPower[side] > maxPower) {
            val -= internalNextPower[side] - maxPower;
            internalNextPower[side] = maxPower;
            if (val < 0) {
                val = 0;
            }
        }

        dbgEnergyInput[side] += val;

        return val;
    }

    public void requestEnergy(EnumFacing from, int amount) {
        step();

        if (this.container.pipe instanceof IPipeTransportPowerHook) {
            nextPowerQuery[from.ordinal()] += ((IPipeTransportPowerHook) this.container.pipe).requestEnergy(from, amount);
        } else {
            nextPowerQuery[from.ordinal()] += amount;
        }
    }

    @Override
    public void initialize() {
        currentDate = container.getWorld().getTotalWorldTime();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        for (int i = 0; i < 6; ++i) {
            powerQuery[i] = nbttagcompound.getInteger("powerQuery[" + i + "]");
            nextPowerQuery[i] = nbttagcompound.getInteger("nextPowerQuery[" + i + "]");
            internalPower[i] = nbttagcompound.getInteger("internalPower[" + i + "]");
            internalNextPower[i] = nbttagcompound.getInteger("internalNextPower[" + i + "]");
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);

        for (int i = 0; i < 6; ++i) {
            nbttagcompound.setInteger("powerQuery[" + i + "]", powerQuery[i]);
            nbttagcompound.setInteger("nextPowerQuery[" + i + "]", nextPowerQuery[i]);
            nbttagcompound.setDouble("internalPower[" + i + "]", internalPower[i]);
            nbttagcompound.setDouble("internalNextPower[" + i + "]", internalNextPower[i]);
        }
    }

    /** Client-side handler for receiving power updates from the server;
     *
     * @param packetPower */
    public void handlePowerPacket(PacketPowerUpdate packetPower) {
        displayPower = packetPower.displayPower;
        displayFlow = packetPower.displayFlow;
        overload = packetPower.overload ? OVERLOAD_TICKS : 0;
    }

    public boolean isQueryingPower() {
        for (int d : powerQuery) {
            if (d > 0) {
                return true;
            }
        }

        return false;
    }

    static {
        MAX_POWER = 48 * TransportConstants.PIPE_POWER_BASE_CAP;
        
        powerCapacities.put(PipePowerWood.class, MAX_POWER);
        powerCapacities.put(PipePowerStone.class, 2 * TransportConstants.PIPE_POWER_BASE_CAP);
        powerCapacities.put(PipePowerIron.class, 8 * TransportConstants.PIPE_POWER_BASE_CAP);
        powerCapacities.put(PipePowerGold.class, 32 * TransportConstants.PIPE_POWER_BASE_CAP);
        powerCapacities.put(PipePowerDiamond.class, 32 * TransportConstants.PIPE_POWER_BASE_CAP);

        powerResistances.put(PipePowerWood.class, 0.0F);
        powerResistances.put(PipePowerStone.class, 0.025F);
        powerResistances.put(PipePowerIron.class, 0.0625F);
        powerResistances.put(PipePowerGold.class, 0.0125F);
        powerResistances.put(PipePowerDiamond.class, 0.0125F);

        // TODO: Deprecated
        powerResistances.put(PipePowerCobblestone.class, 0.05F);
        powerResistances.put(PipePowerSandstone.class, 0.0125F);
        powerResistances.put(PipePowerQuartz.class, 0.0125F);
        powerResistances.put(PipePowerEmerald.class, 0.0F);
        powerCapacities.put(PipePowerCobblestone.class, 1 * TransportConstants.PIPE_POWER_BASE_CAP);
        powerCapacities.put(PipePowerEmerald.class, 32 * TransportConstants.PIPE_POWER_BASE_CAP);
        powerCapacities.put(PipePowerSandstone.class, 4 * TransportConstants.PIPE_POWER_BASE_CAP);
        powerCapacities.put(PipePowerQuartz.class, 8 * TransportConstants.PIPE_POWER_BASE_CAP);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("PipeTransportPower (" + maxPower + " RF/t)");
        for (EnumFacing face : EnumFacing.VALUES) {
            int ord = face.ordinal();
            left.add(" - " + face.getName2() + " " + displayPower[ord]);
        }
        // left.add("- internalPower: " + Arrays.toString(internalPower) + " <- " + Arrays.toString(internalNextPower));
        // left.add("- powerQuery: " + Arrays.toString(powerQuery) + " <- " + Arrays.toString(nextPowerQuery));
        // left.add("- energy: IN " + Arrays.toString(dbgEnergyInput) + ", OUT " + Arrays.toString(dbgEnergyOutput));
        // left.add("- energy: OFFERED " + Arrays.toString(dbgEnergyOffered));

        // int[] totalPowerQuery = new int[6];
        // for (int i = 0; i < 6; ++i) {
        // if (internalPower[i] > 0) {
        // for (int j = 0; j < 6; ++j) {
        // if (j != i && powerQuery[j] > 0) {
        // Object ep = providers[j];
        // if (ep instanceof IPipeTile || ep instanceof IEnergyReceiver || ep instanceof IEnergyHandler) {
        // totalPowerQuery[i] += powerQuery[j];
        // }
        // }
        // }
        // }
        // }
        //
        // left.add("- totalPowerQuery: " + Arrays.toString(totalPowerQuery));
    }
}
