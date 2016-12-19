package buildcraft.builders.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.VecUtil;

public class RenderQuarry extends TileEntitySpecialRenderer<TileQuarry> {
    public static final LaserData_BC8.LaserType FRAME;
    public static final LaserData_BC8.LaserType FRAME_BOTTOM;
    public static final LaserData_BC8.LaserType DRILL;
    public static final LaserData_BC8.LaserType LASER;

    static {
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            FRAME_BOTTOM = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/quarry/drill");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 4) };
            LaserData_BC8.LaserRow end = null;
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            DRILL = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/quarry/laser");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            LASER = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
    }

    public RenderQuarry() {}

    @Override
    public void renderTileEntityAt(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage) {

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("quarry");

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        if (tile.min != null && tile.max != null) {
            double yOffset = 1;

            if (tile.currentTask != null && tile.currentTask instanceof TileQuarry.TaskBreakBlock) {
                TileQuarry.TaskBreakBlock currentTask1 = (TileQuarry.TaskBreakBlock) tile.currentTask;
                BlockPos pos = currentTask1.pos;

                if (tile.drillPos == null) {
                    LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(LASER, VecUtil.convertCenter(tile.getPos()), VecUtil.convertCenter(pos), 1 / 16D), buffer);
                } else {
                    yOffset = (double) currentTask1.getPower() / currentTask1.getTarget();
                    if (yOffset < 0.9) {
                        yOffset = 1 - yOffset / 0.9;
                    } else {
                        yOffset = (yOffset - 0.9) / 0.1;
                    }
                }
            }

            yOffset += 4 / 16D;

            if (tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
                Vec3d interpolatedPos = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos).scale(partialTicks));

                LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.max.getZ() + 12 / 16D),//
                        1 / 16D, true, true, 0), buffer);
                LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.min.getZ() + 4 / 16D),//
                        1 / 16D, true, true, 0), buffer);
                LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(tile.max.getX() + 12 / 16D, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0), buffer);
                LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(tile.min.getX() + 4 / 16D, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0), buffer);
                LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME_BOTTOM,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1 + 4 / 16D, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0), buffer);
                LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(DRILL,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1 + yOffset, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + yOffset, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0), buffer);
            }
        }

        buffer.setTranslation(0, 0, 0);
        buffer.setTranslation(0, 0, 0);

        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        if (tile.min != null && tile.max != null && tile.currentTask != null && tile.currentTask instanceof TileQuarry.TaskAddFrame) {
            TileQuarry.TaskAddFrame currentTask = (TileQuarry.TaskAddFrame) tile.currentTask;
            int index = tile.getFramePoses().indexOf(currentTask.pos);
            if (index > 1) {
                double progress = (double) currentTask.getPower() / currentTask.getTarget() * (index - 1) / tile.getFramePoses().size();
                double progress1 = (progress >= 0 && progress <= 0.25) ? progress * 4 ://
                    (progress >= 0.25 && progress <= 0.5) ? 1 ://
                        (progress >= 0.5 && progress <= 0.75) ? 1 - (progress - 0.5) * 4 ://
                            (progress >= 0.75 && progress <= 1) ? 0 : -1 /* not possible */;
                double progress2 = (progress >= 0 && progress <= 0.25) ? 1 : (progress >= 0.25 && progress <= 0.5) ? 1 - (progress - 0.25) * 4 : (progress >= 0.5 && progress <= 0.75) ? 0 : (progress >= 0.75 && progress <= 1) ? (progress - 0.75) * 4
                    : -1 /* not possible */;
                double xProgress = -1;
                double zProgress = -1;
                EnumFacing side = tile.getWorld().getBlockState(tile.getPos()).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite();
                BlockPos firstPos = tile.getPos().offset(side);
                switch (side) {
                    case SOUTH:
                        if (firstPos.getX() == tile.min.getX()) {
                            xProgress = 1 - progress2;
                            zProgress = progress1;
                        } else {
                            xProgress = progress2;
                            zProgress = progress1;
                        }
                        break;
                    case WEST:
                        if (firstPos.getZ() == tile.min.getZ()) {
                            xProgress = 1 - progress1;
                            zProgress = 1 - progress2;
                        } else {
                            xProgress = 1 - progress1;
                            zProgress = progress2;
                        }
                        break;
                    case NORTH:
                        if (firstPos.getX() == tile.min.getX()) {
                            xProgress = 1 - progress2;
                            zProgress = 1 - progress1;
                        } else {
                            xProgress = progress2;
                            zProgress = 1 - progress1;
                        }
                        break;
                    case EAST:
                        if (firstPos.getZ() == tile.min.getZ()) {
                            xProgress = progress1;
                            zProgress = 1 - progress2;
                        } else {
                            xProgress = progress1;
                            zProgress = progress2;
                        }
                        break;
                }
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

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileQuarry tile) {
        return true;
    }
}
