package buildcraft.core.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TilePowerConsumerTester extends TileBC_Neptune implements IMjReceiver, IDebuggable {

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    private long lastReceived;
    private long totalReceived;

    public TilePowerConsumerTester(BlockPos pos, BlockState blockState) {
        super(BCCoreBlocks.powerTesterTile.get(), pos, blockState);
        caps.addProvider(mjCaps);
    }

    @Override
//    public void readFromNBT(CompoundTag nbt)
    public void load(CompoundTag nbt) {
//        super.readFromNBT(nbt);
        super.load(nbt);
        lastReceived = nbt.getLong("last");
        totalReceived = nbt.getLong("total");
    }

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
//        nbt = super.writeToNBT(nbt);
        super.saveAdditional(nbt);
        nbt.putLong("last", lastReceived);
        nbt.putLong("total", totalReceived);
//        return nbt;
    }

    // IMjReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return 100000 * MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        if (!simulate) {
            lastReceived = microJoules;
            totalReceived += microJoules;
        }
        return 0;
    }

    // IDebuggable

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("");
        left.add(MutableComponent.create(new LiteralContents("")));
//        left.add("Last received = " + LocaleUtil.localizeMj(lastReceived));
        left.add(MutableComponent.create(new LiteralContents("Last received = ")).append(LocaleUtil.localizeMjComponent(lastReceived)));
//        left.add("Total received = " + LocaleUtil.localizeMj(totalReceived));
        left.add(MutableComponent.create(new LiteralContents("Total received = ")).append(LocaleUtil.localizeMjComponent(totalReceived)));
    }
}
