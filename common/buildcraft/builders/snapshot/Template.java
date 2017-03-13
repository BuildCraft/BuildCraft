package buildcraft.builders.snapshot;

import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Template extends Snapshot {
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
        public final Rotation rotation;
        public final BlockPos size;
        public final List<BlockPos> toBreak = new ArrayList<>();
        public final List<BlockPos> toPlace = new ArrayList<>();

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
            size = RotationUtil.rotateBlockPos(getSnapshot().size, rotation);
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        BlockPos blockPos = RotationUtil.rotateBlockPos(new BlockPos(x, y, z), rotation)
                                .add(basePos)
                                .add(RotationUtil.rotateBlockPos(offset, rotation));
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
