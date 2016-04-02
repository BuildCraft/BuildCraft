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
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.*;

public class PipeTransportPower extends PipeTransport implements IDebuggable {
    public static enum LossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;
    }

    public static final Map<Class<? extends Pipe<?>>, Integer> powerCapacities = new HashMap<>();
    public static final Map<Class<? extends Pipe<?>>, Float> powerResistances = new HashMap<>();
    public static final Map<Class<? extends Pipe<?>>, Float> powerLosses = new HashMap<>();
    public static LossMode lossMode = LossMode.LOSSLESS;
    public static boolean canExplode = false;

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
    public int powerLimit = Integer.MAX_VALUE;
    public float powerResistance;

    public int[] dbgEnergyInput = new int[6];
    public int[] dbgEnergyOutput = new int[6];
    public int[] dbgEnergyOffered = new int[6];

    private final AverageInt[] powerAverage = new AverageInt[6];
    private final TileEntity[] tiles = new TileEntity[6];
    private final Object[] providers = new Object[6];

    private boolean needsInit = true;

    private int[] powerQuery = new int[6];
    private int energyInputTick = 0;

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
        maxPower = powerCapacities.get(pipeClass);

        switch (lossMode) {
            case PERCENTAGE:
                powerResistance = powerResistances.get(pipeClass);
                break;
            case ABSOLUTE:
                powerResistance = powerLosses.get(pipeClass);
                break;
        }

        if (canExplode) {
            powerLimit = maxPower;
            maxPower = MAX_POWER;
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

        if (PipeTransportPower.canExplode) {
            if (overload >= 3) {
                destroyPipe();
                return;
            }
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

        if (PipeTransportPower.canExplode) {
            if (energyInputTick > powerLimit || overload > 0) {
                overload++;
            } else {
                overload = 0;
            }
        } else {
            overload += highestPower > (maxPower * 0.95F) ? 1 : -1;
            if (overload < 0) {
                overload = 0;
            }
            if (overload > OVERLOAD_TICKS) {
                overload = OVERLOAD_TICKS;
            }
        }

        energyInputTick = 0;

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

        switch (lossMode) {
            case LOSSLESS:
                internalNextPower[side] += val;
                break;
            case PERCENTAGE:
                internalNextPower[side] += val * (1.0F - powerResistance);
                break;
            case ABSOLUTE:
                if (val < powerResistance) {
                    return 0;
                }
                internalNextPower[side] += val - powerResistance;
                break;
        }

        if (internalNextPower[side] > maxPower) {
            val -= internalNextPower[side] - maxPower;
            internalNextPower[side] = maxPower;
            if (val < 0) {
                val = 0;
            }
        }

        dbgEnergyInput[side] += val;
        energyInputTick += val;

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

    public static void setPowerCapacity(Class<? extends Pipe<?>> pipe, int capacity) {
        powerCapacities.put(pipe, capacity);
        int mp = 0;
        for (int p : powerCapacities.values()) {
            if (p > mp) {
                mp = p;
            }
        }
        MAX_POWER = mp;
    }

    public static void setPowerResistance(Class<? extends Pipe<?>> pipe, float resistance) {
        powerResistances.put(pipe, resistance);
    }

    public static void setPowerLoss(Class<? extends Pipe<?>> pipe, float loss) {
        powerLosses.put(pipe, loss);
    }

    static {
        setPowerCapacity(PipePowerCobblestone.class, 1 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerStone.class, 2 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerSandstone.class, 2 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerWood.class, 4 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerQuartz.class, 8 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerIron.class, 16 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerGold.class, 32 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerEmerald.class, 32 * TransportConstants.PIPE_POWER_BASE_CAP);
        setPowerCapacity(PipePowerDiamond.class, 64 * TransportConstants.PIPE_POWER_BASE_CAP);

        setPowerResistance(PipePowerCobblestone.class, 0.05F);
        setPowerResistance(PipePowerStone.class, 0.025F);
        setPowerResistance(PipePowerWood.class, 0.0F);
        setPowerResistance(PipePowerSandstone.class, 0.0125F);
        setPowerResistance(PipePowerQuartz.class, 0.0125F);
        setPowerResistance(PipePowerIron.class, 0.0125F);
        setPowerResistance(PipePowerGold.class, 0.003125F);
        setPowerResistance(PipePowerEmerald.class, 0.0F);
        setPowerResistance(PipePowerDiamond.class, 0.0F);

        setPowerLoss(PipePowerCobblestone.class, 0.25F);
        setPowerLoss(PipePowerStone.class, 0.25F);
        setPowerLoss(PipePowerSandstone.class, 0.2F);
        setPowerLoss(PipePowerWood.class, 0F);
        setPowerLoss(PipePowerQuartz.class, 2F);
        setPowerLoss(PipePowerIron.class, 2F);
        setPowerLoss(PipePowerGold.class, 4F);
        setPowerLoss(PipePowerEmerald.class, 0F);
        setPowerLoss(PipePowerDiamond.class, 0.5F);
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
