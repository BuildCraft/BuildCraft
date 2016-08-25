package buildcraft.robotics;

import buildcraft.lib.CachedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class ZonePlannerMapRenderer {
    public static ZonePlannerMapRenderer instance = new ZonePlannerMapRenderer();
    private Map<ZonePlannerMapChunkKey, Integer> chunkListIndexes = new CachedMap<>(ZonePlannerMapData.TIMEOUT);

    private static void vertex(double x, double y, double z, double u, double v) {
        GL11.glTexCoord2d(u, v);
        GL11.glVertex3d(x, y, z);
    }

    public void drawBlockCuboid(double x, double y, double z, double height, double radius) {
        double rX = radius;
        double rY = height * 0.5;
        double rZ = radius;

        y -= rY;

        GL11.glNormal3d(0, 1, 0);
        vertex(x - rX, y + rY, z + rZ, 0, 0);
        vertex(x + rX, y + rY, z + rZ, 0, 1);
        vertex(x + rX, y + rY, z - rZ, 1, 1);
        vertex(x - rX, y + rY, z - rZ, 1, 0);

        GL11.glNormal3d(0, -1, 0);
        vertex(x - rX, y - rY, z - rZ, 0, 0);
        vertex(x + rX, y - rY, z - rZ, 0, 1);
        vertex(x + rX, y - rY, z + rZ, 1, 1);
        vertex(x - rX, y - rY, z + rZ, 1, 0);

        GL11.glNormal3d(-1, 0, 0);
        vertex(x - rX, y - rY, z + rZ, 0, 0);
        vertex(x - rX, y + rY, z + rZ, 0, height);
        vertex(x - rX, y + rY, z - rZ, 1, height);
        vertex(x - rX, y - rY, z - rZ, 1, 0);

        GL11.glNormal3d(1, 0, 0);
        vertex(x + rX, y - rY, z - rZ, 0, 0);
        vertex(x + rX, y + rY, z - rZ, 0, height);
        vertex(x + rX, y + rY, z + rZ, 1, height);
        vertex(x + rX, y - rY, z + rZ, 1, 0);

        GL11.glNormal3d(0, 0, -1);
        vertex(x - rX, y - rY, z - rZ, 0, 0);
        vertex(x - rX, y + rY, z - rZ, 0, height);
        vertex(x + rX, y + rY, z - rZ, 1, height);
        vertex(x + rX, y - rY, z - rZ, 1, 0);

        GL11.glNormal3d(0, 0, 1);
        vertex(x + rX, y - rY, z + rZ, 0, 0);
        vertex(x + rX, y + rY, z + rZ, 0, height);
        vertex(x - rX, y + rY, z + rZ, 1, height);
        vertex(x - rX, y - rY, z + rZ, 1, 0);
    }

    public void drawBlockCuboid(double x, double y, double z, double height) {
        drawBlockCuboid(x, y, z, height, 0.5);
    }

    public void drawBlockCuboid(double x, double y, double z) {
        drawBlockCuboid(x, y, z, 1);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public int drawChunk(ZonePlannerMapChunkKey zonePlannerMapChunkKey) {
        if(chunkListIndexes.containsKey(zonePlannerMapChunkKey)) {
            return chunkListIndexes.get(zonePlannerMapChunkKey);
        }
        int listIndexEmpty = GL11.glGenLists(1);
        GL11.glNewList(listIndexEmpty, GL11.GL_COMPILE);
        // noting, wait for chunk data
        GL11.glEndList();
        chunkListIndexes.put(zonePlannerMapChunkKey, listIndexEmpty);
        ZonePlannerMapDataClient.instance.getChunk(Minecraft.getMinecraft().theWorld, zonePlannerMapChunkKey, zonePlannerMapChunk -> {
            int listIndex = GL11.glGenLists(1);
            GL11.glNewList(listIndex, GL11.GL_COMPILE);
            GL11.glBegin(GL11.GL_QUADS);
            for(BlockPos pos : zonePlannerMapChunk.data.keySet()) {
                int color = zonePlannerMapChunk.data.get(pos);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color >> 0) & 0xFF;
                int a = (color >> 24) & 0xFF;
                GL11.glColor4d(r / (double)0xFF, g / (double)0xFF, b / (double)0xFF, a / (double)0xFF);
                drawBlockCuboid(zonePlannerMapChunkKey.chunkPos.chunkXPos * 16 + pos.getX(), pos.getY(), zonePlannerMapChunkKey.chunkPos.chunkZPos * 16 + pos.getZ(), pos.getY());
            }
            GL11.glEnd();
            GL11.glEndList();
            chunkListIndexes.put(zonePlannerMapChunkKey, listIndex);
        });
        return listIndexEmpty;
    }
}
