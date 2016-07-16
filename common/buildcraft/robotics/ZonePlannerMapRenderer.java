package buildcraft.robotics;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class ZonePlannerMapRenderer {
    public static ZonePlannerMapRenderer instance = new ZonePlannerMapRenderer();
    private Map<Pair<Integer, Integer>, Integer> chunkListIndexes = new HashMap<>();

    private void renderCube(double x, double y, double z) {
        double rX = 1;
        double rY = 1;
        double rZ = 1;

        GL11.glNormal3d(0, 1, 0);
        GL11.glVertex3d(x - rX, y + rY, z + rZ);
        GL11.glVertex3d(x + rX, y + rY, z + rZ);
        GL11.glVertex3d(x + rX, y + rY, z - rZ);
        GL11.glVertex3d(x - rX, y + rY, z - rZ);

        GL11.glNormal3d(0, -1, 0);
        GL11.glVertex3d(x - rX, y - rY, z - rZ);
        GL11.glVertex3d(x + rX, y - rY, z - rZ);
        GL11.glVertex3d(x + rX, y - rY, z + rZ);
        GL11.glVertex3d(x - rX, y - rY, z + rZ);

        GL11.glNormal3d(-1, 0, 0);
        GL11.glVertex3d(x - rX, y - rY, z + rZ);
        GL11.glVertex3d(x - rX, y + rY, z + rZ);
        GL11.glVertex3d(x - rX, y + rY, z - rZ);
        GL11.glVertex3d(x - rX, y - rY, z - rZ);

        GL11.glNormal3d(1, 0, 0);
        GL11.glVertex3d(x + rX, y - rY, z - rZ);
        GL11.glVertex3d(x + rX, y + rY, z - rZ);
        GL11.glVertex3d(x + rX, y + rY, z + rZ);
        GL11.glVertex3d(x + rX, y - rY, z + rZ);

        GL11.glNormal3d(0, 0, -1);
        GL11.glVertex3d(x - rX, y - rY, z - rZ);
        GL11.glVertex3d(x - rX, y + rY, z - rZ);
        GL11.glVertex3d(x + rX, y + rY, z - rZ);
        GL11.glVertex3d(x + rX, y - rY, z - rZ);

        GL11.glNormal3d(0, 0, 1);
        GL11.glVertex3d(x + rX, y - rY, z + rZ);
        GL11.glVertex3d(x + rX, y + rY, z + rZ);
        GL11.glVertex3d(x - rX, y + rY, z + rZ);
        GL11.glVertex3d(x - rX, y - rY, z + rZ);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public int drawChunk(World world, int chunkX, int chunkZ) {
        Pair<Integer, Integer> chunkPosPair = Pair.of(chunkX, chunkZ);
        if(chunkListIndexes.containsKey(chunkPosPair)) {
            return chunkListIndexes.get(chunkPosPair);
        }
        int listIndexEmpty = GL11.glGenLists(1);
        GL11.glNewList(listIndexEmpty, GL11.GL_COMPILE);
        // noting, wait for chunk data
        GL11.glEndList();
        chunkListIndexes.put(chunkPosPair, listIndexEmpty);
        ZonePlannerMapDataClient.instance.getChunk(world, chunkX, chunkZ, zonePlannerMapChunk -> {
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
                renderCube(chunkX * 16 + pos.getX(), pos.getY(), chunkZ * 16 + pos.getZ());
            }
            GL11.glEnd();
            GL11.glEndList();
            chunkListIndexes.put(chunkPosPair, listIndex);
        });
        return listIndexEmpty;
    }
}
