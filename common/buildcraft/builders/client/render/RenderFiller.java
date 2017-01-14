package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.client.render.ItemRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.FastTESR;
import org.apache.commons.lang3.tuple.MutableTriple;

public class RenderFiller extends FastTESR<TileFiller> {
    @Override
    public void renderTileEntityFast(TileFiller tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        for (MutableTriple<BlockPos, ItemStack, Long> placeTask : tile.clientPlaceTasks) {
            ItemStack stack = placeTask.getMiddle();
            Vec3d prevPos = tile.prevClientPlaceTasks.stream()
                    .filter(placeTaskLocal -> placeTaskLocal.getLeft().equals(placeTask.getLeft()))
                    .map(tile::getTaskPos)
                    .findFirst()
                    .orElse(tile.getTaskPos(MutableTriple.of(tile.getPos(), ItemStack.EMPTY, 0L)));
            Vec3d pos = prevPos.add(tile.getTaskPos(placeTask).subtract(prevPos).scale(partialTicks));
            ItemRenderUtil.renderItemStack(x - tile.getPos().getX() + pos.xCoord, y - tile.getPos().getY() + pos.yCoord, z - tile.getPos().getZ() + pos.zCoord, stack, EnumFacing.SOUTH, vb);
            ItemRenderUtil.endItemBatch();
        }

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
