package buildcraft.core.blueprints.iterator;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.utils.Utils.AxisOrder;

/** Will build the blueprint in a given order, never deviating from it. Simple and quick, ideal for a single building
 * tile which stays in exactly the same place. */
public class BptBuilderOrdered implements IBptBuilder {
    /** The order that we will go through. This is only used for {@link #internalInit()} stuffs though. */
    private final AxisOrder order;
    private final List<BuildingSlot> buildingSlots = Lists.newArrayList();

    public BptBuilderOrdered(BlueprintBase bluePrint, World world, BlockPos pos, AxisOrder order) {
        super(bluePrint, world, pos);
        this.order = order;
    }

    @Override
    protected void internalInit() {

    }

    @Override
    protected BuildingSlot reserveNextBlock(World world, BlockPos closestTo) {
        // This never reserves anything, so this call is effectively useless
        return null;
    }

    @Override
    protected BuildingSlot getNextBlock(World world, TileAbstractBuilder inv) {
        // Actually gets the next block to build in the list.
        return null;
    }
}
