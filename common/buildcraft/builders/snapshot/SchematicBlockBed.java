package buildcraft.builders.snapshot;

import com.google.common.collect.Lists;
import net.minecraft.block.BedBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class SchematicBlockBed extends SchematicBlockDefault {
    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        switch (blockState.getValue(BedBlock.PART)) {
            case HEAD:
                return Lists.newArrayList();
            case FOOT:
                return super.computeRequiredItems();
        }
        throw new RuntimeException("[builders.snapshot] Unexpected BedBlock part: [" + blockState.getValue(BedBlock.PART) + "]");
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        switch (blockState.getValue(BedBlock.PART)) {
            case HEAD:
                return true;
            case FOOT:
                boolean placeResult = super.build(world, blockPos);
                if (placeResult) {
                    BlockPos otherPos = blockPos.relative(blockState.getValue(BedBlock.FACING));
                    blockState = blockState.setValue(BedBlock.PART, BedPart.HEAD);
                    placeResult = super.build(world, otherPos);
                    blockState = blockState.setValue(BedBlock.PART, BedPart.FOOT);
                }
                return placeResult;
        }
        return false;
    }
}
