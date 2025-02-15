package buildcraft.builders.snapshot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SchematicBlockBed extends SchematicBlockDefault {
    @NotNull
    @Override
    public List<ItemStack> computeRequiredItems() {
        return switch (blockState.getValue(BedBlock.PART)) {
            case HEAD -> Lists.newArrayList();
            case FOOT -> super.computeRequiredItems();
        };
    }

    @Override
    public boolean build(Level world, BlockPos blockPos) {
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
