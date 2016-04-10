package buildcraft.lib.engine;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.mj.*;
import buildcraft.core.lib.BlockTileCache;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.lib.data.DataTemplate;
import buildcraft.lib.mj.helpers.MjSimpleProducer;
import buildcraft.lib.tile.TileBuildCraft_BC8;

public abstract class TileEngineBase_BC8 extends TileBuildCraft_BC8 implements ITickable {
    protected static final DataTemplate TEMPLATE_BASE;

    static {
        TEMPLATE_BASE = new DataTemplate.Builder()//
                .addEnum("direction", EnumFacing.class)//
                .build();
    }

    protected final MjSimpleProducer mjProducer = createProducer();
    private EnumFacing currentDirection = EnumFacing.UP;
    // Keep a buffer of what tiles are infront of us.
    protected final BlockTileCache[] infrontBuffer = new BlockTileCache[getMaxEngineCarryDist()];
    // refreshed from above, but is guaranteed to be non-null and contain non-null.
    private TileEngineBase_BC8[] enginesInFront = new TileEngineBase_BC8[0];

    public TileEngineBase_BC8(int stages) {
        super(stages);
        // Just make sure
        remakeTileCaches();
    }

    protected abstract MjSimpleProducer createProducer();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_MACHINE && facing == getCurrentDirection()) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_MACHINE && facing == getCurrentDirection()) return (T) mjProducer;
        return super.getCapability(capability, facing);
    }

    @Override
    public DataTemplate getTemplateFor(int stage) {
        if (stage == 0) return TEMPLATE_BASE;
        return null;
    }

    @Override
    public void readFromNBT(int stage, NBTTagCompound nbt) {
        currentDirection = NBTUtils.readEnum(nbt.getTag("direction"), EnumFacing.class);
    }

    @Override
    public NBTTagCompound writeToNBT(int stage) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("direction", NBTUtils.writeEnum(currentDirection));
        return nbt;
    }

    @Override
    public void update() {
        if (cannotUpdate()) return;
        // Refresh our engine caches. Every tick for some reason.
        // Except that this is really cheap to do, and we don't want to leave straggling tile entities.
        int num = 0;
        TileEngineBase_BC8[] engines = new TileEngineBase_BC8[infrontBuffer.length];
        for (BlockTileCache cache : infrontBuffer) {
            if (cache == null) break;
            // if the cache is not loaded then don't even bother checking.
            if (!cache.exists()) break;
            TileEntity tile = cache.getTile();
            if (tile instanceof TileEngineBase_BC8) {
                TileEngineBase_BC8 forwardEngine = (TileEngineBase_BC8) tile;
                // No infinite loops
                if (forwardEngine.getCurrentDirection() != currentDirection) break;
                // Just make sure we can carry over- we don't want to carry power over a redstone engine.
                if (canCarryOver(forwardEngine) && forwardEngine.canCarryOver(this)) {
                    engines[num++] = forwardEngine;
                } else break;
            } else {
                break;
            }
        }
        enginesInFront = Arrays.copyOf(engines, num);
        mjProducer.tick();
    }

    private void remakeTileCaches() {
        if (cannotUpdate()) return;
        BlockPos pos = getPos();
        for (int i = infrontBuffer.length - 1; i >= 0; i--) {
            pos = pos.offset(currentDirection);
            infrontBuffer[i] = new BlockTileCache(getWorld(), pos, false);
        }
        MjAPI.NET_INSTANCE.refreshMachine(mjProducer);
    }

    @Override
    public void validate() {
        super.validate();
        // We can connect to blocks further than 1 block away
        MjAPI.NET_INSTANCE.addOddMachine(mjProducer);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        MjAPI.NET_INSTANCE.removeMachine(mjProducer);
    }

    public EnumFacing getCurrentDirection() {
        return currentDirection;
    }

    public boolean hasRedstoneSignal() {
        return worldObj.isBlockPowered(getPos());
    }

    public abstract EnumEnergyStage getEnergyStage();

    /** Checks to see if this engine has more fuel. Essentially canStartOrContinueBurning but this is shorter. */
    public abstract boolean hasMoreFuel();

    /** @return The maximum number of milliwatts that this engine can supply at this moment, in total. You should NOT
     *         take into account the value given to you from {@link #setCurrentUsed(int)} */
    public abstract int getMaxCurrentlySuppliable();

    /** Sets the current number of milliwatts being used up. This given value will always be less than or equal to
     * {@link #getMaxCurrentlySuppliable()}. Use this to lower your fuel consumption rate.
     * 
     * @param milliwatts */
    public abstract void setCurrentUsed(int milliwatts);

    /** @return How many engines this engine can carry its power output over. This only carries over engines infront
     *         that are facing the same direction. */
    public abstract int getMaxEngineCarryDist();

    /** Checks to see if this can carry power through the given engine. */
    protected abstract boolean canCarryOver(TileEngineBase_BC8 engine);

    public class EngineConnectionLogic implements IConnectionLogic {
        @Override
        public Collection<MjMachineIdentifier> getConnectableMachines(MjMachineIdentifier identifier) {
            BlockPos offset = getPos().offset(getCurrentDirection(), enginesInFront.length);
            MjMachineIdentifier ident = new MjMachineIdentifier(identifier.dimension, offset, getCurrentDirection().getOpposite());
            return ImmutableSet.of(ident);
        }
    }

    public class EngineProducer extends MjSimpleProducer {
        public EngineProducer(EnumMjPowerType powerType) {
            super(TileEngineBase_BC8.this, new EngineConnectionLogic(), null, powerType);
        }

        @Override
        public void onConnectionActivate(IMjConnection connection) {
            super.onConnectionActivate(connection);
        }

        @Override
        public void onConnectionBroken(IMjConnection connection) {
            super.onConnectionBroken(connection);
        }

        @Override
        public void setCurrentUsed(int milliwatts) {
            TileEngineBase_BC8.this.setCurrentUsed(milliwatts);
        }

        @Override
        public int getMaxCurrentlySuppliable() {
            if (!hasRedstoneSignal() || !hasMoreFuel()) return 0;
            return TileEngineBase_BC8.this.getMaxCurrentlySuppliable();
        }
    }
}
