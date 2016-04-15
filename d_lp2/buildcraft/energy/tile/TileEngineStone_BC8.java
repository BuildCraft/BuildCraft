package buildcraft.energy.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.lib.engine.TileEngineBase_BC8;

public class TileEngineStone_BC8 extends TileEngineBase_BC8 {
    public static final int MILLIWATTS_PER_TICK = 1000;
    private final ItemStackHandler itemHandler = new ItemStackHandler(1);
    private ItemStack currentFuel;
    private int ticksLeft = 0;

    public TileEngineStone_BC8() {
        super(1);// All details are known
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing != getCurrentDirection()) return (T) itemHandler;
            return null;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return facing != getCurrentDirection();
        return super.hasCapability(capability, facing);
    }

    @Override
    public EnumEnergyStage getEnergyStage() {
        return EnumEnergyStage.BLUE;
    }

    @Override
    protected boolean canCarryOver(TileEngineBase_BC8 engine) {
        return engine instanceof TileEngineStone_BC8;
    }

    @Override
    public int getMaxEngineCarryDist() {
        return 2;
    }

    @Override
    public void update() {
        if (cannotUpdate()) return;
        if (ticksLeft > 0) {
            ticksLeft--;
            addPower(MILLIWATTS_PER_TICK);
        }
        if (ticksLeft <= 0) {
            ItemStack potentialFuel = itemHandler.extractItem(0, 1, true);
            int value = GameRegistry.getFuelValue(potentialFuel);
            if (value > 0) {
                currentFuel = itemHandler.extractItem(0, 1, false);
                ticksLeft += GameRegistry.getFuelValue(currentFuel);
            }
        }
    }
}
