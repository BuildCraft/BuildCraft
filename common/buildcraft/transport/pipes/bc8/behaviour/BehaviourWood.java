package buildcraft.transport.pipes.bc8.behaviour;

import com.google.common.eventbus.Subscribe;
import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import cofh.api.energy.IEnergyReceiver;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IExtractionManager.IExtractable_BC8;
import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertable_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableFluid;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.api.transport.pipe_bc8.PipeBehaviour_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8.AttemptCreate;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventInteract_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.NetworkUtils;

public class BehaviourWood extends PipeBehaviour_BC8 implements IEnergyReceiver {
    private static final int ENERGY_EXTRACT_SINGLE = 20;
    private static final int MAX_ENERGY = ENERGY_EXTRACT_SINGLE * 64;

    protected static final int POWER_MULTIPLIER = 100;
    protected static final int FLUID_MULTIPLIER = 40;

    /** If the part is the centre then it doesn't extract from anywhere. */
    private EnumPipePart extractionFace = EnumPipePart.CENTER;
    protected final RFBattery battery;

    public BehaviourWood(PipeDefinition_BC8 definition, IPipe_BC8 pipe) {
        super(definition, pipe);
        battery = new RFBattery(MAX_ENERGY, MAX_ENERGY, MAX_ENERGY);
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("extractionFace", NBTUtils.writeEnum(extractionFace));

        NBTTagCompound battNBT = new NBTTagCompound();
        battery.writeToNBT(battNBT);
        nbt.setTag("battery", battNBT);

        return nbt;
    }

    @Override
    public BehaviourWood readFromNBT(NBTBase base) {
        NBTTagCompound nbt = (NBTTagCompound) base;
        extractionFace = NBTUtils.readEnum(nbt.getTag("extractionFace"), EnumPipePart.class);
        battery.readFromNBT(nbt.getCompoundTag("battery"));
        return this;
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        NetworkUtils.writeEnum(buf, extractionFace);
        buf.writeInt(battery.getEnergyStored());
    }

    @Override
    public BehaviourWood readFromByteBuf(ByteBuf buf) {
        extractionFace = NetworkUtils.readEnum(buf, EnumPipePart.class);
        battery.setEnergy(buf.readInt());
        return this;
    }

    @Override
    public int getIconIndex(EnumFacing side) {
        // Icon index 0 is all directions EXCEPT extraction (clear)
        // Icon index 1 is the direction it is extracting from (filled)
        return side == extractionFace.face ? 1 : 0;
    }

    @BCPipeEventHandler
    public void onPipeAttemptConnect(IPipeEventConnection_BC8.AttemptCreate event) {
        if (event instanceof IPipeEventConnection_BC8.AttemptCreate.Pipe) {
            AttemptCreate.Pipe pipeEvent = (AttemptCreate.Pipe) event;
            PipeBehaviour_BC8 other = pipeEvent.with().getBehaviour();
            if (other instanceof BehaviourWood) {
                event.disallow();
            }
        }
    }

    //
    // @Subscribe
    // public void onRecievePower(IPipeEventPowered powered) {
    // double excess = internalStorage.insertPower(powered.getPipe().getTile().getWorld(), powered.getMj(), false);
    // powered.useMj(powered.getMj() - excess, false);
    // }

    @BCPipeEventHandler
    public void onTick(IPipeEvent_BC8.Tick tick) {
        if (battery.getEnergyStored() > ENERGY_EXTRACT_SINGLE) {
            int energyRequired = extract(battery.getEnergyStored());
            battery.extractEnergy(energyRequired, false);
        }
    }

    /** @param items The number of items you can extract
     * @return The amount of power used */
    private int extract(int availableEnergy) {
        IConnection_BC8 connection = pipe.getConnections().get(extractionFace.face);
        if (connection == null) return 0;
        IExtractable_BC8 extractable = pipe.getConnections().get(extractionFace).getExtractor();
        IInsertable_BC8 insertable = PipeAPI_BC8.INSERTION_MANAGER.getInsertableFor(pipe);

        IPipeContentsEditable contents = extractType(availableEnergy, insertable, extractable);
        if (contents == null) return 0;

        int energyRequired = getEnergyCost(contents);

        boolean inserted;
        if (contents instanceof IPipeContentsEditableItem) {
            inserted = insertable.tryInsertItems((IPipeContentsEditableItem) contents, pipe, extractionFace.face.getOpposite(), false);
        } else {
            inserted = insertable.tryInsertFluid((IPipeContentsEditableFluid) contents, pipe, extractionFace.face.getOpposite(), false);
        }
        if (!inserted) throw new IllegalStateException("Cannot NOT insert!");
        return energyRequired;
    }

    /** Extracts the correct type based on the definition's type.
     * 
     * @param availableEnergy The maximum energy that can be used.
     * @param pipeInsertable The insertable that will accept whatever you extract (Don't insert it here, but test to see
     *            if your contents can be inserted and extract whatever CAN be inserted)
     * @param extractable The extractable to extract from
     * @return The contents you took away from the extractable, or null if you could not extract anything */
    protected IPipeContentsEditable extractType(int availableEnergy, IInsertable_BC8 pipeInsertable, IExtractable_BC8 extractable) {
        return null;
    }

    protected int getEnergyCost(IPipeContentsEditable contents) {
        if (contents instanceof IPipeContentsEditableItem) {
            ItemStack stack = ((IPipeContentsEditableItem) contents).cloneItemStack();
            return ENERGY_EXTRACT_SINGLE * stack.stackSize;
        } else if (contents instanceof IPipeContentsEditableFluid) {
            FluidStack stack = ((IPipeContentsEditableFluid) contents).cloneFluidStack();
            return ENERGY_EXTRACT_SINGLE * stack.amount / FLUID_MULTIPLIER;
        } else {
            throw new IllegalStateException("Was not an expected type! (" + contents.getClass() + ")");
        }
    }

    @Subscribe
    public void disconnectBlock(IPipeEventConnection_BC8.Destroy disconnect) {
        if (disconnect.getFace() == extractionFace.face) {
            extractionFace = EnumPipePart.CENTER;
            selectNewDirection();
        }
    }

    @Subscribe
    public void connectBlock(IPipeEventConnection_BC8.Create connect) {
        if (extractionFace == null) {
            selectNewDirection();
        }
    }

    @Subscribe
    public void onWrench(IPipeEventInteract_BC8.UseWrench wrench) {
        selectNewDirection();
    }

    private void selectNewDirection() {
        if (pipe.getWorld().isRemote) {
            return;
        }
        EnumPipePart part = extractionFace;
        if (part == EnumPipePart.CENTER) part = part.next();
        int left = 6;
        while (left > 0) {
            part = part.next();
            left--;
            IConnection_BC8 connection = pipe.getConnections().get(part.face);
            if (isValidExtraction(connection)) {
                extractionFace = part;
                pipe.sendClientUpdate(this);
                pipe.sendRenderUpdate();
                return;
            }
        }
        extractionFace = EnumPipePart.CENTER;
        pipe.sendClientUpdate(this);
        pipe.sendRenderUpdate();
    }

    protected boolean isValidExtraction(IConnection_BC8 connection) {
        if (connection == null) return false;
        IExtractable_BC8 extractable = connection.getExtractor();
        return definition.type == PipeAPI_BC8.PIPE_TYPE_ITEM ? extractable.givesItems() : extractable.givesFluids();
    }

    // IEnergyReciever

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return pipe.getConnections().get(from) == null;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return battery.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return battery.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return battery.getMaxEnergyStored();
    }
}
