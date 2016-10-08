package buildcraft.robotics.client.render;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.lib.client.sprite.DynamicTextureBC;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.data.WorldPos;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlannerMapChunkKey;
import buildcraft.robotics.zone.ZonePlannerMapDataClient;

public class RenderZonePlanner extends TileEntitySpecialRenderer<TileZonePlanner> {
    public static final Cache<WorldPos, DynamicTextureBC> TEXTURES;
    private static final int TEXTURE_WIDTH = 10;
    private static final int TEXTURE_HEIGHT = 8;

    static {
        TEXTURES = CacheBuilder.newBuilder()//
                .expireAfterWrite(5, TimeUnit.MINUTES)//
                .removalListener(RenderZonePlanner::onRemove)//
                .build();
    }

    private static void onRemove(RemovalNotification<WorldPos, DynamicTextureBC> notification) {
        DynamicTextureBC texture = notification.getValue();
        if (texture != null) {
            texture.deleteGlTexture();
        }
    }

    @Override
    public final void renderTileEntityAt(TileZonePlanner tile, double x, double y, double z, float partialTicks, int destroyStage) {

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("zone");
        
        double offset = 0.001;
        double minX = 3 / 16D - offset;
        double maxX = 13 / 16D + offset;
        double minY = 5 / 16D - offset;
        double maxY = 13 / 16D + offset;
        double minZ = -offset;
        double maxZ = 1 + offset;

        EnumFacing side = tile.getWorld().getBlockState(tile.getPos()).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite();

        DynamicTextureBC texture = getTexture(tile, side);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        texture.updateTexture();
        texture.bindGlTexture();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(x, y, z);

        Vec3d min = new Vec3d(0, 0, 0);
        Vec3d max = new Vec3d(1, 1, 1);

        float minU = 0;
        float maxU = texture.getMaxU();
        float minV = 0;
        float maxV = texture.getMaxV();

        switch (side) {
            case NORTH:
                min = new Vec3d(minX, minY, maxZ);
                max = new Vec3d(maxX, maxY, maxZ);
                break;
            case EAST:
                min = new Vec3d(minZ, minY, minX);
                max = new Vec3d(minZ, maxY, maxX);
                break;
            case SOUTH:
                min = new Vec3d(minX, minY, minZ);
                max = new Vec3d(maxX, maxY, minZ);
                break;
            case WEST:
            default:
                min = new Vec3d(maxZ, minY, minX);
                max = new Vec3d(maxZ, maxY, maxX);
                break;
        }

        MutableVertex vert = new MutableVertex();

        vert.colouri(-1);
        vert.lighti(0xF, 0xF);

        vert.positiond(min.xCoord, min.yCoord, min.zCoord).texf(minU, minV).render(buffer);
        vert.positiond(max.xCoord, min.yCoord, max.zCoord).texf(maxU, minV).render(buffer);
        vert.positiond(max.xCoord, max.yCoord, max.zCoord).texf(maxU, maxV).render(buffer);
        vert.positiond(min.xCoord, max.yCoord, min.zCoord).texf(minU, maxV).render(buffer);

        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Nonnull
    private static DynamicTextureBC getTexture(final TileZonePlanner tile, final EnumFacing side) {
        Callable<DynamicTextureBC> textureGetter = () -> {
            return createTexture(tile, side);
        };
        try {
            DynamicTextureBC texture = TEXTURES.get(new WorldPos(tile), textureGetter);
            if (texture == null) {
                throw new NullPointerException("Somehow generated a null texture!");
            }
            return texture;

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static DynamicTextureBC createTexture(TileZonePlanner tile, EnumFacing side) {
        DynamicTextureBC texture = new DynamicTextureBC(TEXTURE_WIDTH, TEXTURE_HEIGHT);
        for (int textureX = 0; textureX < TEXTURE_WIDTH; textureX++) {
            for (int textureY = 0; textureY < TEXTURE_HEIGHT; textureY++) {
                int posX = -1;
                int posZ = -1;
                int scale = 4;
                int offset1 = (textureX - TEXTURE_WIDTH / 2) * scale;
                int offset2 = (textureY - TEXTURE_HEIGHT / 2) * scale;
                switch (side) {
                    case NORTH:
                        posX = tile.getPos().getX() + offset1;
                        posZ = tile.getPos().getZ() - offset2;
                        break;
                    case EAST:
                        posX = tile.getPos().getX() + offset2;
                        posZ = tile.getPos().getZ() + offset1;
                        break;
                    case SOUTH:
                        posX = tile.getPos().getX() + offset1;
                        posZ = tile.getPos().getZ() + offset2;
                        break;
                    case WEST:
                    default:
                        posX = tile.getPos().getX() - offset2;
                        posZ = tile.getPos().getZ() + offset1;
                        break;
                }
                int finalTextureX = textureX;
                int finalTextureY = textureY;
                ChunkPos chunkPos = new ChunkPos(posX >> 4, posZ >> 4);
                int finalPosX = posX;
                int finalPosZ = posZ;
                texture.setColor(finalTextureX, finalTextureY, -1);
                ZonePlannerMapChunkKey key = new ZonePlannerMapChunkKey(chunkPos, tile.getWorld().provider.getDimension(), tile.getLevel());
                ZonePlannerMapDataClient.INSTANCE.loadChunk(tile.getWorld(), key, (chunk) -> {
                    int colour = chunk.getColour(finalPosX, finalPosZ) | 0xFF_00_00_00;
                    texture.setColor(finalTextureX, finalTextureY, colour);
                });
            }
        }
        return texture;
    }
}
