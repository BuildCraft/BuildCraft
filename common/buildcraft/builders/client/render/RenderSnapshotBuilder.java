package buildcraft.builders.client.render;

import buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;

public class RenderSnapshotBuilder {
    public static <T extends ITileForSnapshotBuilder> void render(
            SnapshotBuilder<T> snapshotBuilder,
            World world,
            BlockPos tilePos,
            double x,
            double y,
            double z,
            float partialTicks,
            VertexBuffer vb
    ) {
        for (SnapshotBuilder<T>.PlaceTask placeTask : snapshotBuilder.clientPlaceTasks) {
            Vec3d prevPos = snapshotBuilder.prevClientPlaceTasks.stream()
                    .filter(renderTaskLocal -> renderTaskLocal.pos.equals(placeTask.pos))
                    .map(snapshotBuilder::getPlaceTaskItemPos)
                    .findFirst()
                    .orElse(snapshotBuilder.getPlaceTaskItemPos(snapshotBuilder.new PlaceTask(tilePos, Collections.emptyList(), 0L)));
            Vec3d pos = prevPos.add(snapshotBuilder.getPlaceTaskItemPos(placeTask).subtract(prevPos).scale(partialTicks));
            for (ItemStack item : placeTask.items) {
                ItemRenderUtil.renderItemStack(
                        x - tilePos.getX() + pos.xCoord,
                        y - tilePos.getY() + pos.yCoord,
                        z - tilePos.getZ() + pos.zCoord,
                        item,
                        world.getCombinedLight(tilePos, 0),
                        EnumFacing.SOUTH,
                        vb
                );
            }
            ItemRenderUtil.endItemBatch();
        }

        Vec3d robotPos = snapshotBuilder.robotPos;
        if (robotPos != null) {
            if (snapshotBuilder.prevRobotPos != null) {
                robotPos = snapshotBuilder.prevRobotPos.add(robotPos.subtract(snapshotBuilder.prevRobotPos).scale(partialTicks));
            }

            RenderEntity.renderOffsetAABB(
                    new AxisAlignedBB(
                            robotPos.subtract(VecUtil.VEC_HALF),
                            robotPos.add(VecUtil.VEC_HALF)
                    ),
                    x - tilePos.getX(),
                    y - tilePos.getY(),
                    z - tilePos.getZ()
            );

            vb.setTranslation(x - tilePos.getX(), y - tilePos.getY(), z - tilePos.getZ());

            for (SnapshotBuilder.BreakTask breakTask : snapshotBuilder.clientBreakTasks) {
                LaserRenderer_BC8.renderLaserDynamic(
                        new LaserData_BC8(
                                BuildCraftLaserManager.POWERS[(int) Math.round(
                                        MathUtil.clamp(
                                                breakTask.power * 1D / breakTask.getTarget(),
                                                0D,
                                                1D
                                        ) * (BuildCraftLaserManager.POWERS.length - 1)
                                )],
                                robotPos,
                                new Vec3d(breakTask.pos).add(VecUtil.VEC_HALF),
                                1 / 16D
                        ),
                        vb
                );
            }
        }

        vb.setTranslation(0, 0, 0);
    }
}
