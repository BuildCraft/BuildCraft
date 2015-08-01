/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.lib.EntityResizableCuboid;

// Deprecated in favour of RenderEntityCuboid
@Deprecated
public final class RenderEntityBlock extends Render {
    public static RenderEntityBlock INSTANCE = new RenderEntityBlock();

    private RenderEntityBlock() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class RenderInfo {
        public double minX;
        public double minY;
        public double minZ;
        public double maxX;
        public double maxY;
        public double maxZ;
        public IBlockState blockState = Blocks.sand.getDefaultState();
        public ResourceLocation resource;
        public TextureAtlasSprite texture = null;
        public TextureAtlasSprite[] textureArray = null;
        public boolean[] renderSide = new boolean[6];
        public float light = -1f;
        public int brightness = -1;

        public RenderInfo() {
            setRenderAllSides();
        }

        public RenderInfo(IBlockState state, TextureAtlasSprite[] texture) {
            this();
            this.blockState = state;
            this.textureArray = texture;
        }

        public RenderInfo(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            this();
            setBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public float getBlockBrightness(IBlockAccess iblockaccess, BlockPos pos) {
            return blockState.getBlock().getMixedBrightnessForBlock(iblockaccess, pos);
        }

        public final void setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public final void setRenderSingleSide(int side) {
            Arrays.fill(renderSide, false);
            renderSide[side] = true;
        }

        public final void setRenderAllSides() {
            Arrays.fill(renderSide, true);
        }

        public void rotate() {
            double temp = minX;
            minX = minZ;
            minZ = temp;

            temp = maxX;
            maxX = maxZ;
            maxZ = temp;
        }

        public void reverseX() {
            double temp = minX;
            minX = 1 - maxX;
            maxX = 1 - temp;
        }

        public void reverseZ() {
            double temp = minZ;
            minZ = 1 - maxZ;
            maxZ = 1 - temp;
        }
    }

    @Override
    public void doRender(Entity entity, double i, double j, double k, float f, float f1) {
        doRenderBlock((EntityResizableCuboid) entity, i, j, k);
    }

    public void doRenderBlock(EntityResizableCuboid entity, double i, double j, double k) {
        if (entity.isDead) {
            return;
        }

        shadowSize = entity.shadowSize;
        World world = entity.worldObj;
        RenderInfo util = new RenderInfo();
        if (entity.blockState != null)
            util.blockState = entity.blockState;
        util.resource = entity.resource;
        if (entity.texture != null)
            util.texture = entity.texture;

        for (int iBase = 0; iBase < entity.xSize; ++iBase) {
            for (int jBase = 0; jBase < entity.ySize; ++jBase) {
                for (int kBase = 0; kBase < entity.zSize; ++kBase) {

                    util.minX = 0;
                    util.minY = 0;
                    util.minZ = 0;

                    double remainX = entity.xSize - iBase;
                    double remainY = entity.ySize - jBase;
                    double remainZ = entity.zSize - kBase;

                    util.maxX = remainX > 1.0 ? 1.0 : remainX;
                    util.maxY = remainY > 1.0 ? 1.0 : remainY;
                    util.maxZ = remainZ > 1.0 ? 1.0 : remainZ;
                    // GlStateManager.enableTexture2D();
                    GlStateManager.enableRescaleNormal();
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float) i, (float) j, (float) k);
                    GL11.glRotatef(entity.rotationX, 1, 0, 0);
                    GL11.glRotatef(entity.rotationY, 0, 1, 0);
                    GL11.glRotatef(entity.rotationZ, 0, 0, 1);
                    GL11.glTranslatef(iBase, jBase, kBase);

                    int lightX, lightY, lightZ;

                    lightX = (int) (Math.floor(entity.posX) + iBase);
                    lightY = (int) (Math.floor(entity.posY) + jBase);
                    lightZ = (int) (Math.floor(entity.posZ) + kBase);

                    GL11.glDisable(GL11.GL_LIGHTING);
                    renderBlock(util, world, 0, 0, 0, new BlockPos(lightX, lightY, lightZ), false, true);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glPopMatrix();
                    GlStateManager.disableRescaleNormal();

                }
            }
        }
    }

    /** Render a render info by its state, ignoring any textures you might have set */
    public void renderBlock(RenderInfo info) {
        BlockRendererDispatcher renderBlocks = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel model = renderBlocks.getBlockModelShapes().getModelForState(info.blockState);
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        worldRenderer.startDrawingQuads();
        renderBlocks.getBlockModelRenderer().renderModelStandard(null, model, info.blockState.getBlock(), BlockPos.ORIGIN, worldRenderer, false);
        Tessellator.getInstance().draw();
    }

    public void renderBlock(RenderInfo info, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating) {
        renderBlock(info, blockAccess, x, y, z, new BlockPos(x, y, z), doLight, doTessellating);
    }

    /** Render 6 sides according to the lengths that have been set in the render info */
    public void renderBlock(RenderInfo info, IBlockAccess blockAccess, double x, double y, double z, BlockPos lightPos, boolean doLight,
            boolean doTessellating) {

        GlStateManager.pushMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        // BlockRendererDispatcher renderBlocks = Minecraft.getMinecraft().getBlockRendererDispatcher();
        renderer.startDrawingQuads();
        renderer.setVertexFormat(DefaultVertexFormats.BLOCK);
        renderer.setTranslation(-x, -y, -z);

        ResourceLocation resource = info.resource;
        if (resource == null) {
            resource = TextureMap.locationBlocksTexture;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(resource);

        GlStateManager.translate(-x, -y, -z);
        // GlStateManager.scale(1 / 16D, 1 / 16D, 1 / 16D);
        // GlStateManager.scale(info.maxX - info.minX, info.maxY - info.minY, info.maxZ - info.minZ);
        // IBlockState state = info.blockState;
        // FIXME: Change RenderEntityBlock in some way. Perhaps remove it entirely?
        // DebugWorldAccessor.getDebugWorld().world = blockAccess;
        // renderBlocks.renderBlock(state, DebugWorldAccessor.getPositionForBlockState(state),
        // DebugWorldAccessor.getDebugWorld(), renderer);
        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
