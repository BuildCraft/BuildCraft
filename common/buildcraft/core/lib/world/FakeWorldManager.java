package buildcraft.core.lib.world;

import java.util.EnumMap;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.Utils;

public class FakeWorldManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final BlockPos min, max;
    private final FakeWorld world;
    // private final RenderGlobal renderGlobal;
    // private final ChunkRenderDispatcher chunkBatcher;
    // private final Map<BlockPos, RenderChunk> chunks = Maps.newHashMap();
    private final EnumMap<EnumWorldBlockLayer, Tessellator> tessMap = Maps.newEnumMap(EnumWorldBlockLayer.class);
    private final EnumMap<EnumWorldBlockLayer, Integer> displayListMap = Maps.newEnumMap(EnumWorldBlockLayer.class);

    public FakeWorldManager(FakeWorld world) {
        this.world = world;
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.values()) {
            tessMap.put(layer, new Tessellator(1 << 16));
        }
        world.tick();

        // Minecraft mc = Minecraft.getMinecraft();
        // renderGlobal = mc.renderGlobal;
        // chunkBatcher = new ChunkRenderDispatcher();
        min = new BlockPos(-64, 0, -64);
        max = new BlockPos(64, 8, 64);
        // for (int x = min.getX(); x < max.getX(); x += 16) {
        // for (int y = min.getY(); y < max.getY(); y += 16) {
        // for (int z = min.getZ(); z < max.getZ(); z += 16) {
        // BlockPos pos = new BlockPos(x, y, z);
        // RenderChunk chunk = new ListedRenderChunk(world, renderGlobal, min, 0);
        // chunks.put(pos, chunk);
        // chunkBatcher.updateChunkNow(chunk);
        // }
        // }
        // }
        // while (chunkBatcher.runChunkUploads(0));
    }

    public void deleteAll() {
        for (int list : displayListMap.values()) {
            GL11.glDeleteLists(list, 1);
        }
        displayListMap.clear();
    }

    public void renderWorld(double mouseX, double mouseY, double sf, BlockPos offset) {
        // Prepare
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.pushMatrix();
        GL11.glTranslated(0, 0, 2000 - sf);
        // GlStateManager.scale(sf, sf, sf);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        float aspect = mc.displayWidth / (float) mc.displayHeight;
        Project.gluPerspective(mc.gameSettings.fovSetting, aspect, 0.01f, mc.gameSettings.renderDistanceChunks * 1000);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glRotated(mouseY, 1, 0, 0);
        GL11.glRotated(mouseX, 0, 1, 0);

        RenderUtils.translate(Utils.multiply(Utils.convert(offset), -0.5));

        renderAll();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
    }

    private void renderAll() {
        GlStateManager.disableAlpha();
        // Solids
        EnumWorldBlockLayer layer = EnumWorldBlockLayer.SOLID;
        renderAllChunks(layer);

        // Cutouts (That have mipmapping applied)
        layer = EnumWorldBlockLayer.CUTOUT_MIPPED;
        GlStateManager.enableAlpha();
        renderAllChunks(layer);

        // Cutouts (That don't have mipmapping applied)
        layer = EnumWorldBlockLayer.CUTOUT;
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        renderAllChunks(layer);

        // Cutout tear-down
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        // Translucent (Partial alpha)
        layer = EnumWorldBlockLayer.TRANSLUCENT;
        if (mc.gameSettings.fancyGraphics) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            renderAllChunks(layer);
            GlStateManager.disableBlend();
        } else {
            renderAllChunks(layer);
        }
    }

    private void renderAllChunks(EnumWorldBlockLayer layer) {
        Tessellator tess = tessMap.get(layer);
        WorldRenderer renderer = tess.getWorldRenderer();
        if (displayListMap.containsKey(layer)) {
            GL11.glCallList(displayListMap.get(layer));
            // tess.draw();
        } else if (world.hasDeployed) {
            int list = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(list, GL11.GL_COMPILE);
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            renderAllBlocks(layer, renderer);
            tess.draw();
            GL11.glEndList();
            displayListMap.put(layer, list);
        }
    }

    private void renderAllBlocks(EnumWorldBlockLayer layer, WorldRenderer renderer) {
        for (ChunkCoordIntPair ccip : Utils.allChunksFor(min, max)) {
            if (world.getChunkProvider().chunkExists(ccip.chunkXPos, ccip.chunkZPos)) {
                Chunk chunk = world.getChunkFromChunkCoords(ccip.chunkXPos, ccip.chunkZPos);
                boolean hasObj = false;
                for (int h : chunk.getHeightMap()) {
                    if (h > 1) {
                        hasObj = true;
                        break;
                    }
                }
                if (!hasObj) {
                    continue;
                }
                for (BlockPos pos : Utils.allInChunk(ccip)) {
                    renderBlock(pos, layer, renderer);
                }
            }
        }
    }

    private void renderBlock(BlockPos pos, EnumWorldBlockLayer layer, WorldRenderer renderer) {
        IBlockState actualState = world.getBlockState(pos);
        Block block = actualState.getBlock();
        if (block == Blocks.air || block == null) {
            return;
        }

        if (!block.canRenderInLayer(layer)) {
            return;
        }

        actualState = block.getActualState(actualState, world, pos);

        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();

        IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(actualState);

        if (model == null || model.getParticleTexture() == null || model.getGeneralQuads() == null) {
            return;
        }

        boolean checkSides = pos.getY() > 0;
        try {
            dispatcher.getBlockModelRenderer().renderModelStandard(world, model, block, pos, renderer, checkSides);
        } catch (Throwable t) {
            BCLog.logger.warn("The model from the block " + block + " was invalid for layer " + layer, t);
        }
    }
}
