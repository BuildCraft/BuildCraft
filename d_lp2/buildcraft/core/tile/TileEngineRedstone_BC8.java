package buildcraft.core.tile;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.mj.EnumMjPowerType;
import buildcraft.api.mj.helpers.MjSimpleProducer;
import buildcraft.core.lib.utils.AverageDouble;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.lib.data.DataTemplate;
import buildcraft.lib.engine.TileEngineBase_BC8;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    // TODO: Fix these numbers as they are probably completely wrong
    public static final int[] MILLIWATTS_PROVIDED = { 35, 50, 75, 100, 0 };

    protected static final DataTemplate TEMPLATE_REDSTONE;

    static {
        TEMPLATE_REDSTONE = TileEngineBase_BC8.TEMPLATE_BASE.toBuilder()//
                .addEnum("stage", EnumEnergyStage.class)//
                .build();
    }

    private EnumEnergyStage stage = EnumEnergyStage.BLUE;
    private AverageDouble powerAvg = new AverageDouble(10);
    private long lastChange = 0;

    public TileEngineRedstone_BC8() {
        this(1);
    }

    protected TileEngineRedstone_BC8(int saveStages) {
        super(saveStages);
    }

    @Override
    protected MjSimpleProducer createProducer() {
        return new EngineProducer(EnumMjPowerType.REDSTONE);
    }

    @Override
    public DataTemplate getTemplateFor(int stage) {
        if (stage == 0) return TEMPLATE_REDSTONE;
        return super.getTemplateFor(stage);
    }

    @Override
    public NBTTagCompound writeToNBT(int stage) {
        NBTTagCompound nbt = super.writeToNBT(stage);
        if (stage == 0) {
            nbt.setTag("stage", NBTUtils.writeEnum(this.stage));
            nbt.setTag("average", powerAvg.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(int stage, NBTTagCompound nbt) {
        super.readFromNBT(stage, nbt);
        if (stage == 0) {
            this.stage = NBTUtils.readEnum(nbt.getTag("stage"), EnumEnergyStage.class);
            powerAvg.deserializeNBT(nbt.getCompoundTag("average"));
        }
    }

    @Override
    public EnumEnergyStage getEnergyStage() {
        return stage;
    }

    @Override
    public boolean hasMoreFuel() {
        return true;// We always have more fuel
    }

    @Override
    public int getMaxCurrentlySuppliable() {
        return MILLIWATTS_PROVIDED[stage.ordinal()];
    }

    @Override
    public void setCurrentUsed(int milliwatts) {
        int supply = getMaxCurrentlySuppliable();
        if (supply == 0) {
            powerAvg.push(0);
        } else {
            double beingUsed = milliwatts / (double) supply;
            powerAvg.push(beingUsed * 2);
        }
    }

    @Override
    public int getMaxEngineCarryDist() {
        return 1;
    }

    @Override
    protected boolean canCarryOver(TileEngineBase_BC8 engine) {
        return engine instanceof TileEngineRedstone_BC8;
    }

    @Override
    public void update() {
        super.update();
        if (cannotUpdate()) return;
        powerAvg.tick();
        double average = powerAvg.getAverage();
        if (average > 1) {
            if (worldObj.getTotalWorldTime() > lastChange + 100) {
                if (stage != EnumEnergyStage.OVERHEAT) {
                    stage = EnumEnergyStage.VALUES[stage.ordinal() + 1];
                    lastChange = worldObj.getTotalWorldTime();
                    redrawBlock();
                }
            }
        } else if (average < 0.5) {
            if (worldObj.getTotalWorldTime() > lastChange + 20) {
                if (stage != EnumEnergyStage.BLUE) {
                    stage = EnumEnergyStage.VALUES[stage.ordinal() - 1];
                    lastChange = worldObj.getTotalWorldTime();
                    redrawBlock();
                }
            }
        }
    }
}
