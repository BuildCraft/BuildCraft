package buildcraft.builders.snapshot;

import buildcraft.lib.misc.data.Box;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Template extends Snapshot {
    public BlockPos size;
    public BlockPos offset = BlockPos.ORIGIN;
    public boolean[][][] data;

    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        // noinspection unchecked
        return (SnapshotBuilder<T>) new TemplateBuilder((ITileForTemplateBuilder) tile);
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.TEMPLATE;
    }

    public class BuildingInfo {
        public final BlockPos basePos;
        public final List<BlockPos> toBreak = new ArrayList<>();
        public final List<BlockPos> toPlace = new ArrayList<>();

        public BuildingInfo(BlockPos basePos) {
            this.basePos = basePos;
            for (int z = 0; z < size.getZ(); z++) {
                for (int y = 0; y < size.getY(); y++) {
                    for (int x = 0; x < size.getX(); x++) {
                        BlockPos blockPos = new BlockPos(x, y, z).add(basePos).add(offset);
                        if (!data[x][y][z]) {
                            toBreak.add(blockPos);
                        } else {
                            toPlace.add(blockPos);
                        }
                    }
                }
            }
        }

        public Template getSnapshot() {
            return Template.this;
        }

        public Box getBox() {
            return new Box(basePos, basePos.add(getSnapshot().size));
        }
    }
}
