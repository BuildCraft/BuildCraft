package buildcraft.robotics.zone;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import buildcraft.lib.client.model.MutableVertex;
import buildcraft.robotics.zone.ZonePlannerMapChunk.MapColourData;

public enum ZonePlannerMapRenderer {
    INSTANCE;

    private static final Cache<ZonePlannerMapChunkKey, Integer> CHUNK_GL_CACHE;
    private static final Set<ZonePlannerMapChunkKey> GENERATING = Collections.synchronizedSet(new HashSet<>());
    private final MutableVertex vertex = new MutableVertex();

    static {
        CHUNK_GL_CACHE = CacheBuilder.newBuilder()//
                .expireAfterAccess(20, TimeUnit.SECONDS)//
                .removalListener(ZonePlannerMapRenderer::onRemove)//
                .build();
    }

    private static void onRemove(RemovalNotification<ZonePlannerMapChunkKey, Integer> notification) {
        Integer val = notification.getValue();
        if (val != null) {
            GL11.glDeleteLists(val.intValue(), 1);
        }
    }

    private void vertex(VertexBuffer builder, double x, double y, double z) {
        vertex.positiond(x, y, z);
        vertex.render(builder);
    }

    public void drawBlockCuboid(VertexBuffer builder, double x, double y, double z, double height, double radius) {
        double rX = radius;
        double rY = height * 0.5;
        double rZ = radius;

        y -= rY;

        vertex.normalf(0, 1, 0);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x - rX, y + rY, z - rZ);

        vertex.normalf(-1, 0, 0);
        vertex.multColourd(0.6);
        vertex(builder, x - rX, y - rY, z + rZ);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x - rX, y + rY, z - rZ);
        vertex(builder, x - rX, y - rY, z - rZ);

        vertex.normalf(1, 0, 0);
        vertex(builder, x + rX, y - rY, z - rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex(builder, x + rX, y - rY, z + rZ);
        vertex.multColourd(1 / 0.6);

        vertex.normalf(0, 0, -1);
        vertex.multColourd(0.8);
        vertex(builder, x - rX, y - rY, z - rZ);
        vertex(builder, x - rX, y + rY, z - rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x + rX, y - rY, z - rZ);

        vertex.normalf(0, 0, 1);
        vertex(builder, x + rX, y - rY, z + rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x - rX, y - rY, z + rZ);
        vertex.multColourd(1 / 0.8);
    }

    public void drawBlockCuboid(VertexBuffer builder, double x, double y, double z, double height) {
        drawBlockCuboid(builder, x, y, z, height, 0.5);
    }

    public void drawBlockCuboid(VertexBuffer builder, double x, double y, double z) {
        drawBlockCuboid(builder, x, y, z, 1);
    }

    public int getChunkGlList(ZonePlannerMapChunkKey key) {
        Integer val = CHUNK_GL_CACHE.getIfPresent(key);
        if (val == null) {
            if (GENERATING.add(key)) {
                genChunk(key);
            }
            return -1;
        } else {
            GENERATING.remove(key);
            return val.intValue();
        }
    }

    private void genChunk(ZonePlannerMapChunkKey key) {
        ZonePlannerMapDataClient.INSTANCE.getChunk(Minecraft.getMinecraft().world, key, chunk -> {
            VertexBuffer builder = Tessellator.getInstance().getBuffer();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);// TODO: normals
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    MapColourData data = chunk.getData(x, z);
                    if (data != null) {
                        vertex.colouri(data.colour >> 16, data.colour >> 8, data.colour, data.colour >> 24);
                        drawBlockCuboid(builder, key.chunkPos.getXStart() + x, data.posY, key.chunkPos.getZStart() + z, data.posY);
                    }
                }
            }
            int listIndex = GL11.glGenLists(1);
            GL11.glNewList(listIndex, GL11.GL_COMPILE);
            Tessellator.getInstance().draw();
            GL11.glEndList();
            CHUNK_GL_CACHE.put(key, listIndex);
        });
    }
}
