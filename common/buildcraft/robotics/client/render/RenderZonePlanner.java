package buildcraft.robotics.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.CachedMap;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlannerMapChunkKey;
import buildcraft.robotics.zone.ZonePlannerMapData;
import buildcraft.robotics.zone.ZonePlannerMapDataClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.Map;

public class RenderZonePlanner extends TileEntitySpecialRenderer<TileZonePlanner> {
    public static Map<TileZonePlanner, DynamicTexture> textures = new CachedMap<>(ZonePlannerMapData.TIMEOUT);

    @Override
    public final void renderTileEntityAt(TileZonePlanner tile, double x, double y, double z, float partialTicks, int destroyStage) {
        double minX = 3 / 16D;
        double maxX = 13 / 16D;
        double minY = 5 / 16D;
        double maxY = 13 / 16D;
        double minZ = -0.001;
        double maxZ = 1.001;
        int textureWidth = 10;
        int textureHeight = 8;
        int textureSize = 1 << ((Double.doubleToRawLongBits(Math.max(textureWidth, textureHeight) - 1) >> 52) - 1022);
        double minU = 0;
        double maxU = (double) textureWidth / textureSize;
        double minV = 0;
        double maxV = (double) textureHeight / textureSize;

        EnumFacing side = tile.getWorld().getBlockState(tile.getPos()).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite();

        if(textures.containsKey(tile)) {
            DynamicTexture texture = textures.get(tile);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer buffer = tessellator.getBuffer();
            GlStateManager.bindTexture(texture.getGlTextureId());
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.disableBlend();
            GlStateManager.disableCull();
            if(Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            buffer.setTranslation(x, y, z);

            Vec3d min = new Vec3d(0, 0, 0);
            Vec3d max = new Vec3d(1, 1, 1);

            switch(side) {
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
                    min = new Vec3d(maxZ, minY, minX);
                    max = new Vec3d(maxZ, maxY, maxX);
                    break;
            }

            buffer.pos(min.xCoord, min.yCoord, min.zCoord).color(255, 255, 255, 255).tex(minU, minV).lightmap(0xFF, 0xFF).endVertex();
            buffer.pos(max.xCoord, min.yCoord, max.zCoord).color(255, 255, 255, 255).tex(maxU, minV).lightmap(0xFF, 0xFF).endVertex();
            buffer.pos(max.xCoord, max.yCoord, max.zCoord).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(0xFF, 0xFF).endVertex();
            buffer.pos(min.xCoord, max.yCoord, min.zCoord).color(255, 255, 255, 255).tex(minU, maxV).lightmap(0xFF, 0xFF).endVertex();

            buffer.setTranslation(0, 0, 0);
            tessellator.draw();
            RenderHelper.enableStandardItemLighting();
        } else {
            BufferedImage image = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_RGB);
            if(textures.containsKey(tile)) {
                GlStateManager.deleteTexture(textures.get(tile).getGlTextureId());
            }
            for(int textureX = 0; textureX < textureWidth; textureX++) {
                for(int textureY = 0; textureY < textureHeight; textureY++) {
                    int posX = -1;
                    int posZ = -1;
                    int scale = 1;
                    int offset1 = (textureX - textureWidth / 2) * scale;
                    int offset2 = (textureY - textureHeight / 2) * scale;
                    switch(side) {
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
                            posX = tile.getPos().getX() - offset2;
                            posZ = tile.getPos().getZ() + offset1;
                            break;
                    }
                    int finalTextureX = textureX;
                    int finalTextureY = textureY;
                    ChunkPos chunkPos = new ChunkPos(posX >> 4, posZ >> 4);
                    int finalPosX = posX;
                    int finalPosZ = posZ;
                    ZonePlannerMapDataClient.instance.loadChunk(tile.getWorld(), new ZonePlannerMapChunkKey(chunkPos, tile.getWorld().provider.getDimension(), tile.getLevel()),
                            zonePlannerMapChunk -> zonePlannerMapChunk.data.forEach((pos, color) -> {
                                if(pos.getX() == finalPosX - chunkPos.getXStart() && pos.getZ() == finalPosZ - chunkPos.getZStart()) {
                                    image.setRGB(finalTextureX, finalTextureY, color);
                                    GlStateManager.deleteTexture(textures.get(tile).getGlTextureId());
                                    textures.put(tile, new DynamicTexture(image));
                                }
                            }));
                }
            }
            textures.put(tile, new DynamicTexture(image));
        }
    }
}
