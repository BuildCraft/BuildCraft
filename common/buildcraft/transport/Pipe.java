/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.ActionState;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.internal.IDropControlInventory;
import buildcraft.core.lib.event.IEventBus;
import buildcraft.core.lib.event.IEventBusProvider;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.gates.GateFactory;
import buildcraft.transport.pipes.events.PipeEvent;
import buildcraft.transport.statements.ActionValve.ValveState;

public abstract class Pipe<T extends PipeTransport> implements IDropControlInventory, IPipe, Comparable<Pipe<T>> {
    // TODO: Change this to EventBusProviderASM!
    private static final IEventBusProvider<PipeEvent> eventProvider = PipeEventBus.Provider.INSTANCE;

    public int[] signalStrength = new int[] { 0, 0, 0, 0 };
    public TileGenericPipe container;
    public final T transport;
    public final Item item;
    public boolean[] wireSet = new boolean[] { false, false, false, false };
    public final Gate[] gates = new Gate[EnumFacing.VALUES.length];
    public IEventBus<PipeEvent> eventBus = eventProvider.newBus();

    private boolean initialized = false;
    private boolean scheduleWireUpdate;

    private ArrayList<ActionState> actionStates = new ArrayList<ActionState>();

    public Pipe(T transport, Item item) {
        this.transport = transport;
        this.item = item;

        eventBus.registerHandler(this);
    }

    public void setTile(TileEntity tile) {
        this.container = (TileGenericPipe) tile;
        transport.setTile((TileGenericPipe) tile);
    }

    public void resolveActions() {
        for (Gate gate : gates) {
            if (gate != null) {
                gate.resolveActions();
            }
        }
    }

    public boolean blockActivated(EntityPlayer player, EnumFacing side) {
        return false;
    }

    public void onBlockPlaced() {
        transport.onBlockPlaced();
    }

    public void onBlockPlacedBy(EntityLivingBase placer) {}

    public void onNeighborBlockChange(int blockId) {
        for (EnumFacing face : EnumFacing.values()) {
            transport.onNeighborChange(face);
        }
    }

    public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
        Pipe<?> otherPipe;

        if (tile instanceof IPipeTile) {
            otherPipe = (Pipe<?>) ((IPipeTile) tile).getPipe();
            if (!BlockGenericPipe.isFullyDefined(otherPipe)) {
                return false;
            }

            if (!PipeConnectionBans.canPipesConnect(getClass(), otherPipe.getClass())) {
                return false;
            }
        }

        return transport.canPipeConnect(tile, side);
    }

    /** Should return the textureindex used by the Pipe Item Renderer, as this is done client-side the default
     * implementation might not work if your getTextureIndex(Orienations.Unknown) has logic. Then override this */
    public int getIconIndexForItem() {
        return getIconIndex(null);
    }

    /** Should return the IIconProvider that provides icons for this pipe
     *
     * @return An array of icons */
    @SideOnly(Side.CLIENT)
    public abstract IIconProvider getIconProvider();

    /** Should return the index in the array returned by GetTextureIcons() for a specified direction
     *
     * @param direction - The direction for which the indexed should be rendered. Unknown for pipe center
     *
     * @return An index valid in the array returned by getTextureIcons() */
    public abstract int getIconIndex(EnumFacing direction);

    public void updateEntity() {
        transport.updateEntity();

        actionStates.clear();

        // Update the gate if we have any
        if (!container.getWorld().isRemote) {
            for (Gate gate : gates) {
                if (gate != null) {
                    gate.resolveActions();
                    gate.tick();
                }
            }

            if (scheduleWireUpdate) {
                updateSignalState();
            }
        }
    }

    public void writeToNBT(NBTTagCompound data) {
        transport.writeToNBT(data);

        // Save gate if any
        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            final String key = "Gate[" + i + "]";
            Gate gate = gates[i];
            if (gate != null) {
                NBTTagCompound gateNBT = new NBTTagCompound();
                gate.writeToNBT(gateNBT);
                data.setTag(key, gateNBT);
            } else {
                data.removeTag(key);
            }
        }

        for (int i = 0; i < 4; ++i) {
            data.setBoolean("wireSet[" + i + "]", wireSet[i]);
        }
    }

    public void readFromNBT(NBTTagCompound data) {
        transport.readFromNBT(data);

        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            final String key = "Gate[" + i + "]";
            gates[i] = data.hasKey(key) ? GateFactory.makeGate(this, data.getCompoundTag(key)) : null;
        }

        // Legacy support
        if (data.hasKey("Gate")) {
            transport.container.setGate(GateFactory.makeGate(this, data.getCompoundTag("Gate")), 0);

            data.removeTag("Gate");
        }

        for (int i = 0; i < 4; ++i) {
            wireSet[i] = data.getBoolean("wireSet[" + i + "]");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() {
        transport.initialize();
        updateSignalState();
        initialized = true;
    }

    public void updateSignalState() {
        for (PipeWire c : PipeWire.values()) {
            if (wireSet[c.ordinal()]) {
                updateSignalState(c);
            }
        }
    }

    private void updateSignalState(PipeWire c) {
        int prevStrength = signalStrength[c.ordinal()];
        boolean isBroadcast = false;

        for (Gate g : gates) {
            if (g != null && (g.broadcastSignal & (1 << c.ordinal())) != 0) {
                isBroadcast = true;
                break;
            }
        }

        if (isBroadcast) {
            if (prevStrength < 255) {
                propagateSignalState(c, 255);
            }
        } else {
            List<Pipe<?>> pipes = new ArrayList<Pipe<?>>();
            int maxStrength = 0;

            for (EnumFacing dir : EnumFacing.values()) {
                TileEntity tile = container.getTile(dir);
                if (tile instanceof IPipeTile) {
                    Pipe<?> pipe = (Pipe<?>) ((IPipeTile) tile).getPipe();
                    if (isWireConnectedTo(tile, pipe, c, dir)) {
                        pipes.add(pipe);
                        int pipeStrength = pipe.signalStrength[c.ordinal()];
                        if (pipeStrength > maxStrength) {
                            maxStrength = pipeStrength;
                        }
                    }
                }
            }

            if (maxStrength > prevStrength && maxStrength > 1) {
                signalStrength[c.ordinal()] = maxStrength - 1;
            } else {
                signalStrength[c.ordinal()] = 0;
            }

            if (prevStrength != signalStrength[c.ordinal()]) {
                container.scheduleRenderUpdate();
            }

            if (signalStrength[c.ordinal()] == 0) {
                for (Pipe<?> p : pipes) {
                    if (p.signalStrength[c.ordinal()] > 0) {
                        p.updateSignalState(c);
                    }
                }
            } else {
                for (Pipe<?> p : pipes) {
                    if (p.signalStrength[c.ordinal()] < (signalStrength[c.ordinal()] - 1)) {
                        p.updateSignalState(c);
                    }
                }
            }
        }
    }

    protected void propagateSignalState(PipeWire c, int s) {
        signalStrength[c.ordinal()] = s;
        for (EnumFacing face : EnumFacing.values()) {
            TileEntity tile = container.getTile(face);
            if (tile instanceof IPipeTile) {
                Pipe<?> pipe = (Pipe<?>) ((IPipeTile) tile).getPipe();
                if (isWireConnectedTo(tile, pipe, c, face)) {
                    if (s == 0) {
                        if (pipe.signalStrength[c.ordinal()] > 0) {
                            pipe.updateSignalState(c);
                        }
                    } else {
                        if (pipe.signalStrength[c.ordinal()] < s) {
                            pipe.updateSignalState(c);
                        }
                    }
                }
            }
        }
    }

    public boolean inputOpen(EnumFacing from) {
        return transport.inputOpen(from);
    }

    public boolean outputOpen(EnumFacing to) {
        return transport.outputOpen(to);
    }

    public void onEntityCollidedWithBlock(Entity entity) {}

    public boolean canConnectRedstone() {
        for (Gate gate : gates) {
            if (gate != null) {
                return true;
            }
        }
        return false;
    }

    public int getMaxRedstoneOutput(EnumFacing dir) {
        int output = 0;

        for (EnumFacing side : EnumFacing.VALUES) {
            output = Math.max(output, getRedstoneOutput(side));
            if (side == dir) {
                output = Math.max(output, getRedstoneOutputSide(side));
            }
        }

        return output;
    }

    private int getRedstoneOutput(EnumFacing dir) {
        Gate gate = gates[dir.ordinal()];

        return gate != null ? gate.getRedstoneOutput() : 0;
    }

    private int getRedstoneOutputSide(EnumFacing dir) {
        Gate gate = gates[dir.ordinal()];

        return gate != null ? gate.getSidedRedstoneOutput() : 0;
    }

    public int isPoweringTo(EnumFacing side) {
        EnumFacing o = side.getOpposite();

        TileEntity tile = container.getTile(o);

        if (tile instanceof IPipeTile && container.isPipeConnected(o)) {
            return 0;
        } else {
            return getMaxRedstoneOutput(o);
        }
    }

    public int isIndirectlyPoweringTo(EnumFacing l) {
        return isPoweringTo(l);
    }

    public void randomDisplayTick(Random random) {}

    @Override
    public boolean isWired(PipeWire color) {
        return wireSet[color.ordinal()];
    }

    @Override
    public boolean isWireActive(PipeWire color) {
        return signalStrength[color.ordinal()] > 0;
    }

    @Deprecated
    public boolean hasGate() {
        for (EnumFacing direction : EnumFacing.VALUES) {
            if (hasGate(direction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasGate(EnumFacing side) {
        return container.hasGate(side);
    }

    protected void notifyBlocksOfNeighborChange(EnumFacing side) {
        container.getWorld().notifyBlockOfStateChange(container.getPos().offset(side), BuildCraftTransport.genericPipeBlock);
    }

    protected void updateNeighbors(boolean needSelf) {
        if (needSelf) {
            container.getWorld().notifyNeighborsOfStateChange(container.getPos(), BuildCraftTransport.genericPipeBlock);
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            notifyBlocksOfNeighborChange(side);
        }
    }

    public void dropItem(ItemStack stack) {
        InvUtils.dropItems(container.getWorld(), stack, container.getPos());
    }

    public ArrayList<ItemStack> computeItemDrop() {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();

        for (PipeWire pipeWire : PipeWire.VALUES) {
            if (wireSet[pipeWire.ordinal()]) {
                result.add(pipeWire.getStack());
            }
        }

        for (EnumFacing direction : EnumFacing.VALUES) {
            if (container.hasPipePluggable(direction)) {
                Collections.addAll(result, container.getPipePluggable(direction).getDropItems(container));
            }
        }

        return result;
    }

    public LinkedList<IActionInternal> getActions() {
        LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();

        for (ValveState state : ValveState.VALUES) {
            result.add(BuildCraftTransport.actionValve[state.ordinal()]);
        }

        return result;
    }

    public void resetGates() {
        for (int i = 0; i < gates.length; i++) {
            Gate gate = gates[i];
            if (gate != null) {
                gate.resetGate();
            }
            gates[i] = null;
        }

        scheduleWireUpdate = true;
        container.scheduleRenderUpdate();
    }

    protected void actionsActivated(Collection<StatementSlot> actions) {}

    public TileGenericPipe getContainer() {
        return container;
    }

    public boolean isWireConnectedTo(TileEntity tile, PipeWire color, EnumFacing dir) {
        if (!(tile instanceof IPipeTile)) {
            return false;
        }

        return isWireConnectedTo(tile, (Pipe) (((IPipeTile) tile).getPipe()), color, dir);
    }

    public boolean isWireConnectedTo(TileEntity tile, Pipe pipe, PipeWire color, EnumFacing dir) {
        if (!BlockGenericPipe.isFullyDefined(pipe)) {
            return false;
        }

        if (!pipe.wireSet[color.ordinal()]) {
            return false;
        }

        if (container.hasBlockingPluggable(dir) || pipe.container.hasBlockingPluggable(dir.getOpposite())) {
            return false;
        }

        return pipe.transport instanceof PipeTransportStructure || transport instanceof PipeTransportStructure || Utils.checkPipesConnections(
                container, tile);
    }

    public void dropContents() {
        transport.dropContents();
    }

    public List<ItemStack> getDroppedItems() {
        return transport.getDroppedItems();
    }

    /** If this pipe is open on one side, return it. */
    public EnumFacing getOpenOrientation() {
        int connectionsNum = 0;

        EnumFacing targetOrientation = null;

        for (EnumFacing o : EnumFacing.VALUES) {
            if (container.isPipeConnected(o)) {

                connectionsNum++;

                if (connectionsNum == 1) {
                    targetOrientation = o;
                }
            }
        }

        if (connectionsNum > 1 || connectionsNum == 0) {
            return null;
        }

        return targetOrientation.getOpposite();
    }

    @Override
    public boolean doDrop() {
        return true;
    }

    /** Called when TileGenericPipe.invalidate() is called */
    public void invalidate() {}

    /** Called when TileGenericPipe.validate() is called */
    public void validate() {}

    /** Called when TileGenericPipe.onChunkUnload is called */
    public void onChunkUnload() {}

    public World getWorld() {
        return container.getWorld();
    }

    @Override
    public IPipeTile getTile() {
        return container;
    }

    @Override
    public IGate getGate(EnumFacing side) {
        if (side == null) {
            return null;
        }

        return gates[side.ordinal()];
    }

    private void pushActionState(ActionState state) {
        actionStates.add(state);
    }

    private Collection<ActionState> getActionStates() {
        return actionStates;
    }

    public void scheduleWireUpdate() {
        scheduleWireUpdate = true;
    }

    public int compareTo(Pipe<T> pipe) {
        return 0;
    }
}
