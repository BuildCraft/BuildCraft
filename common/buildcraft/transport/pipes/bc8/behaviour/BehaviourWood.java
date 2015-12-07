package buildcraft.transport.pipes.bc8.behaviour;

import com.google.common.eventbus.Subscribe;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import cofh.api.energy.IEnergyReceiver;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IContentsFilter;
import buildcraft.api.transport.pipe_bc8.IExtractionManager.IExtractable_BC8;
import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertable_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeContents;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableFluid;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditablePower;
import buildcraft.api.transport.pipe_bc8.IPipeHelper.EnumCombiningOp;
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
import buildcraft.transport.pipes.bc8.filter.MaximumContentsFilter;

import io.netty.buffer.ByteBuf;

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
     * @return The number of items extracted */
    private int extract(int availableEnergy) {
        IConnection_BC8 connection = pipe.getConnections().get(extractionFace.face);
        if (connection == null) return 0;
        IExtractable_BC8 extractable = pipe.getConnections().get(extractionFace).getExtractor();
        IInsertable_BC8 insertable = PipeAPI_BC8.INSERTION_MANAGER.getInsertableFor(pipe);

        IContentsFilter insertableFilter = insertable.getFilterForType(getType());
        IContentsFilter maxEnergyFilter = getFilter(availableEnergy);
        IContentsFilter filter = PipeAPI_BC8.PIPE_HELPER.combineFilters(insertableFilter, maxEnergyFilter, EnumCombiningOp.AND);

        IPipeContentsEditable contents = extractable.tryExtract(filter, pipe, extractionFace.face.getOpposite());

        int energyRequired = getEnergyCost(contents);

        boolean inserted = insertable.tryInsert(contents, pipe, extractionFace.face.getOpposite());
        if (!inserted) throw new IllegalStateException("Cannot NOT insert!");
        return energyRequired;
    }

    private IPipeContents getType() {
        if (definition.type == PipeAPI_BC8.PIPE_TYPE_ITEM) {
            return PipeAPI_BC8.PIPE_HELPER.getContentsForItem(new ItemStack(Items.apple));
        } else if (definition.type == PipeAPI_BC8.PIPE_TYPE_FLUID) {
            return PipeAPI_BC8.PIPE_HELPER.getContentsForFluid(new FluidStack(FluidRegistry.WATER, 1));
        } else {
            return PipeAPI_BC8.PIPE_HELPER.getContentsForPower(1);
        }
    }

    private int getEnergyCost(IPipeContentsEditable contents) {
        if (contents instanceof IPipeContentsEditableItem) {
            ItemStack stack = ((IPipeContentsEditableItem) contents).cloneItemStack();
            return ENERGY_EXTRACT_SINGLE * stack.stackSize;
        } else if (contents instanceof IPipeContentsEditableFluid) {
            FluidStack stack = ((IPipeContentsEditableFluid) contents).cloneFluidStack();
            return ENERGY_EXTRACT_SINGLE * stack.amount / FLUID_MULTIPLIER;
        } else if (contents instanceof IPipeContentsEditablePower) {
            int energy = ((IPipeContentsEditablePower) contents).powerHeld();
            return ENERGY_EXTRACT_SINGLE * energy / POWER_MULTIPLIER;
        } else {
            throw new IllegalStateException("Was not an expected type! (" + contents.getClass() + ")");
        }
    }

    /** Gets a contents filter for the maximum energy available. */
    protected IContentsFilter getFilter(int energy) {
        if (definition.type == PipeAPI_BC8.PIPE_TYPE_ITEM) {
            return new MaximumContentsFilter.Item(energy / ENERGY_EXTRACT_SINGLE);
        } else if (definition.type == PipeAPI_BC8.PIPE_TYPE_FLUID) {
            return new MaximumContentsFilter.Fluid(energy * FLUID_MULTIPLIER / ENERGY_EXTRACT_SINGLE);
        } else {
            return new MaximumContentsFilter.Power(energy * POWER_MULTIPLIER / ENERGY_EXTRACT_SINGLE);
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
        return extractable.givesType(getType());
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
