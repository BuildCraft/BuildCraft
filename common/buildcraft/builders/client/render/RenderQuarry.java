package buildcraft.builders.client.render;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.CullTESR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RenderQuarry extends CullTESR<TileQuarry> {
    public static final LaserData_BC8.LaserType FRAME;
    public static final LaserData_BC8.LaserType FRAME_BOTTOM;
    public static final LaserData_BC8.LaserType DRILL;

    static {
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = {
                    new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)
            };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = {
                    new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)
            };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            FRAME_BOTTOM = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/quarry/drill");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = {
                    new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 4)
            };
            LaserData_BC8.LaserRow end = null;
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            DRILL = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
    }

    public RenderQuarry() {}

    @Override
    public void renderTileEntityFast(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        if(tile.min != null && tile.max != null && tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
            Vec3d interpolatedPos = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos).scale(partialTicks));

            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord), new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.max.getZ() + 12 / 16D), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord), new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.min.getZ() + 4 / 16D), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), new Vec3d(tile.max.getX() + 12 / 16D, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), new Vec3d(tile.min.getX() + 4 / 16D, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME_BOTTOM, new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1 + 4 / 16D, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(DRILL, new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1 + 4 / 16D, interpolatedPos.zCoord + 0.5), new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 4 / 16D, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
        }

        if(tile.min != null && tile.max != null && tile.drillPos == null && tile.currentTask != null && tile.currentTask instanceof TileQuarry.TaskBreakBlock) {
            TileQuarry.TaskBreakBlock currentTask = (TileQuarry.TaskBreakBlock) tile.currentTask;
            BlockPos pos = currentTask.pos;

            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE, new Vec3d(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), 1 / 16D), buffer);
        }

        buffer.setTranslation(0, 0, 0);
    }

    @Override
    public void renderTileEntityAt(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage) {
        super.renderTileEntityAt(tile, x, y, z, partialTicks, destroyStage);

        if(tile.min != null && tile.max != null && tile.currentTask != null && tile.currentTask instanceof TileQuarry.TaskAddFrame) {
            TileQuarry.TaskAddFrame currentTask = (TileQuarry.TaskAddFrame) tile.currentTask;
            int index = tile.getFramePoses().indexOf(currentTask.pos);
            if(index > 0) {
                double progress = (double) currentTask.getEnergy() / currentTask.getTarget() * (index - 1) / tile.getFramePoses().size();
                double xProgress = (progress >= 0 && progress <= 0.25) ? progress * 4 :
                                   (progress >= 0.25 && progress <= 0.5) ? 1 :
                                   (progress >= 0.5 && progress <= 0.75) ? 1 - (progress - 0.5) * 4 :
                                   (progress >= 0.75 && progress <= 1) ? 0 : -1 /* not possible */;
                double zProgress = (progress >= 0 && progress <= 0.25) ? 1 :
                                   (progress >= 0.25 && progress <= 0.5) ? 1 - (progress - 0.25) * 4 :
                                   (progress >= 0.5 && progress <= 0.75) ? 0 :
                                   (progress >= 0.75 && progress <= 1) ? (progress - 0.75) * 4 : -1 /* not possible */;
                double xResult = tile.min.getX() + (tile.max.getX() - tile.min.getX()) * xProgress;
                double zResult = tile.min.getZ() + (tile.max.getZ() - tile.min.getZ()) * zProgress;
                ItemStack stack = new ItemStack(BCBuildersBlocks.frame);

                RenderHelper.disableStandardItemLighting();
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.disableTexture2D();
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
                GlStateManager.pushMatrix();
                GlStateManager.translate(xResult + 0.5, tile.getPos().getY(), zResult + 0.5);
                GlStateManager.scale(3, 3, 3);
                Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(TileQuarry tile) {
        return true;
    }
}
