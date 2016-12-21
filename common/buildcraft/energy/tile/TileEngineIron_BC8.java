package buildcraft.energy.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager.IDirtyFuel;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.neptune.IPipeItem;

import buildcraft.energy.BCEnergyGuis;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.fluids.TankProperties;
import buildcraft.lib.misc.EntityUtil;

public class TileEngineIron_BC8 extends TileEngineBase_BC8 {
    public static final int MAX_FLUID = 10_000;

    public static final double COOLDOWN_RATE = 0.05;
    public static final int MAX_COOLANT_PER_TICK = 40;

    public final Tank tankFuel = new Tank("tankFuel", MAX_FLUID, this, this::isValidFuel);
    public final Tank tankCoolant = new Tank("tankCoolant", MAX_FLUID, this, this::isValidCoolant);
    public final Tank tankResidue = new Tank("tankResidue", MAX_FLUID, this, this::isResidue);
    private final TankManager<Tank> tankManager = new TankManager<>(tankFuel, tankCoolant, tankResidue);
    private final IFluidHandlerAdv fluidHandler = new InternalFluidHandler();

    private int penaltyCooling = 0;
    private boolean lastPowered = false;
    private double burnTime;
    private double residueAmount = 0;
    private IFuel currentFuel;

    // TileEntity overrides

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tanks", tankManager.serializeNBT());
        nbt.setInteger("penaltyCooling", penaltyCooling);
        nbt.setDouble("burnTime", burnTime);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tankManager.deserializeNBT(nbt.getCompoundTag("tanks"));
        penaltyCooling = nbt.getInteger("penaltyCooling");
        burnTime = nbt.getDouble("burnTime");
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return facing != currentDirection;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return facing != currentDirection ? (T) fluidHandler : null;
        }
        return super.getCapability(capability, facing);
    }

    // TileEngineBase overrrides

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack current = player.getHeldItem(hand);
        if (current != null) {
            if (EntityUtil.getWrenchHand(player) != null) {
                return false;
            }
            if (current.getItem() instanceof IPipeItem) {
                return false;
            }
            return FluidUtil.interactWithFluidHandler(current, fluidHandler, player);
        }
        BCEnergyGuis.ENGINE_IRON.openGUI(player, getPos());
        return true;
    }

    @Override
    public double getPistonSpeed() {
        switch (getEnergyStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        FluidStack fuel = tankFuel.getFluid();
        return fuel != null && fuel.amount > 0 && penaltyCooling == 0 && isRedstonePowered;
    }

    @Override
    protected void burn() {
        FluidStack fuel = this.tankFuel.getFluid();
        if (currentFuel == null) {
            if (fuel == null) {
                currentFuel = null;
            } else {
                currentFuel = BuildcraftFuelRegistry.fuel.getFuel(fuel.getFluid());
            }
        }

        if (currentFuel == null) {
            return;
        }

        if (penaltyCooling <= 0 && isRedstonePowered) {

            lastPowered = true;

            if (burnTime > 0 || (fuel != null && fuel.amount > 0)) {
                if (burnTime > 0) {
                    burnTime--;
                }
                if (burnTime <= 0) {
                    if (fuel != null) {
                        if (--fuel.amount <= 0) {
                            tankFuel.setFluid(null);
                        }
                        burnTime += currentFuel.getTotalBurningTime() / 1000.0;

                        // If we also produce residue then put it out too
                        if (currentFuel instanceof IDirtyFuel) {
                            IDirtyFuel dirtyFuel = (IDirtyFuel) currentFuel;
                            residueAmount += dirtyFuel.getResidue().amount / 1000.0;
                            if (residueAmount >= 1) {
                                int residue = MathHelper.floor_double(residueAmount);
                                FluidStack residueFluid = dirtyFuel.getResidue().copy();
                                residueFluid.amount = residue;
                                residueAmount -= tankResidue.fill(residueFluid, true);
                            }
                        }
                    } else {
                        currentFuel = null;
                        residueAmount = 0;
                        return;
                    }
                }
                currentOutput = currentFuel.getPowerPerCycle(); // Comment out for constant power
                addPower(currentFuel.getPowerPerCycle());
                heat += currentFuel.getPowerPerCycle() * HEAT_PER_MJ / MjAPI.MJ;// * getBiomeTempScalar();
            }
        } else if (penaltyCooling <= 0) {
            if (lastPowered) {
                lastPowered = false;
                penaltyCooling = 10;
                // 10 tick of penalty on top of the cooling
            }
        }
    }

    @Override
    public void updateHeatLevel() {
        // We update heat ourselves
    }

    @Override
    public boolean isActive() {
        return penaltyCooling <= 0;
    }

    @Override
    public long getMaxPower() {
        return 10_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    public long getCurrentOutput() {
        if (currentFuel == null) {
            return 0;
        } else {
            return currentFuel.getPowerPerCycle();
        }
    }

    // Fluid related

    private boolean isValidFuel(FluidStack fluid) {
        return BuildcraftFuelRegistry.fuel.getFuel(fluid.getFluid()) != null;
    }

    private boolean isValidCoolant(FluidStack fluid) {
        return BuildcraftFuelRegistry.coolant.getCoolant(fluid.getFluid()) != null;
    }

    private boolean isResidue(FluidStack fluid) {
        // If this is the client then we don't have a current fuel- just trust the server that its correct
        if (worldObj != null && worldObj.isRemote) return true;
        if (currentFuel instanceof IDirtyFuel) {
            return fluid.isFluidEqual(((IDirtyFuel) currentFuel).getResidue());
        }
        return false;
    }

    private class InternalFluidHandler implements IFluidHandlerAdv {
        private final IFluidTankProperties[] properties = {//
            new TankProperties(tankFuel, true, false),//
            new TankProperties(tankCoolant, true, false),//
            new TankProperties(tankResidue, false, true),//
        };

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return properties;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = tankFuel.fill(resource, doFill);
            if (filled == 0) {
                filled = tankCoolant.fill(resource, doFill);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return tankResidue.drain(resource, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return tankResidue.drain(maxDrain, doDrain);
        }

        @Override
        public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
            return tankResidue.drain(filter, maxDrain, doDrain);
        }
    }
}
