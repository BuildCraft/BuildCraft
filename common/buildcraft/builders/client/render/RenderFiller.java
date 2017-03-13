package buildcraft.builders.client.render;

import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.FastTESR;

import java.util.Collections;

public class RenderFiller extends FastTESR<TileFiller> {
    @Override
    public void renderTileEntityFast(TileFiller tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        for (TemplateBuilder.PlaceTask placeTask : tile.builder.clientPlaceTasks) {
            Vec3d prevPos = tile.builder.prevClientPlaceTasks.stream()
                    .filter(renderTaskLocal -> renderTaskLocal.pos.equals(placeTask.pos))
                    .map(tile.builder::getPlaceTaskItemPos)
                    .findFirst()
                    .orElse(tile.builder.getPlaceTaskItemPos(tile.builder.new PlaceTask(tile.getPos(), Collections.emptyList(), 0L)));
            Vec3d pos = prevPos.add(tile.builder.getPlaceTaskItemPos(placeTask).subtract(prevPos).scale(partialTicks));
            for (ItemStack item : placeTask.items) {
                ItemRenderUtil.renderItemStack(
                        x - tile.getPos().getX() + pos.xCoord,
                        y - tile.getPos().getY() + pos.yCoord,
                        z - tile.getPos().getZ() + pos.zCoord,
                        item,
                        tile.getWorld().getCombinedLight(tile.getPos(), 0),
                        EnumFacing.SOUTH,
                        vb
                );
            }
            ItemRenderUtil.endItemBatch();
        }

        Vec3d robotPos = tile.builder.robotPos;
        if (robotPos != null) {
            if (tile.builder.prevRobotPos != null) {
                robotPos = tile.builder.prevRobotPos.add(robotPos.subtract(tile.builder.prevRobotPos).scale(partialTicks));
            }

            RenderEntity.renderOffsetAABB(
                    new AxisAlignedBB(
                            robotPos.subtract(VecUtil.VEC_HALF),
                            robotPos.add(VecUtil.VEC_HALF)
                    ),
                    x - tile.getPos().getX(),
                    y - tile.getPos().getY(),
                    z - tile.getPos().getZ()
            );

            vb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

            for (SnapshotBuilder.BreakTask breakTask : tile.builder.clientBreakTasks) {
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

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
