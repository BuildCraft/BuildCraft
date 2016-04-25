/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.client.render;

import java.util.Map;

import javax.vecmath.Vector3f;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.lib.utils.Utils;

public enum RenderUtils {
    INSTANCE;

    private final Map<EnumFacing, Vec3d> rotations = Maps.newEnumMap(EnumFacing.class);

    private RenderUtils() {
        rotations.put(EnumFacing.UP, Utils.VEC_ZERO);
        rotations.put(EnumFacing.DOWN, new Vec3d(180, 0, 0));
        EnumFacing face = EnumFacing.SOUTH;
        for (int a = 0; a < 360; a += 90) {
            rotations.put(face, new Vec3d(90, 0, a));
            face = face.rotateY();
        }
    }

    public static void setGLColorFromInt(int color) {
        setGLColorFromIntPlusAlpha(0xFF_00_00_00 | color);
    }

    public static void setGLColorFromIntPlusAlpha(int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void setWorldRendererRGB(VertexBuffer wr, Vec3d color) {
        wr.color((float) color.xCoord, (float) color.yCoord, (float) color.zCoord, 1f);
    }

    public static void addWorldRendererVertex(VertexBuffer wr, Vec3d vertex) {
        wr.pos(vertex.xCoord, vertex.yCoord, vertex.zCoord);
    }

    public static void putWorldRendererColorMultiplier(VertexBuffer wr, Vec3d color, int index) {
        wr.putColorMultiplier((float) color.xCoord, (float) color.yCoord, (float) color.zCoord, index);
    }

    public static void translate(Vec3d vector) {
        GL11.glTranslated(vector.xCoord, vector.yCoord, vector.zCoord);
    }

    public static void vertex3d(Vec3d vec) {
        GL11.glVertex3d(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static void vertex3f(Vec3d vec) {
        vertex3f(Utils.convertFloat(vec));
    }

    public static void vertex3f(Vector3f vec) {
        GL11.glVertex3f(vec.x, vec.y, vec.z);
    }

    /** Rotates the current matrix to face the specified direction, assuming you want to draw your models upwards */
    public static void rotate(EnumFacing face) {
        rotate(INSTANCE.rotations.get(face));
    }

    public static void rotate(Vec3d rotation) {
        GL11.glRotated(rotation.xCoord, 1, 0, 0);
        GL11.glRotated(rotation.yCoord, 0, 1, 0);
        GL11.glRotated(rotation.zCoord, 0, 0, 1);
    }
}
