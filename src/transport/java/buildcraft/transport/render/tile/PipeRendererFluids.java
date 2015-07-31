package buildcraft.transport.render.tile;

import java.util.Map;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.render.FluidRenderer;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.utils.FluidRenderData;

public class PipeRendererFluids {
    public static final int DISPLAY_STAGES = 100;

    /** Map of FluidID -> Fluid Render Call Lists */
    private static Map<Integer, DisplayFluidList> fluidLists = Maps.newHashMap();

    /** While this class isn't actually completely Immutable, you shouldn't modify any instances after creation */
    static class DisplayFluidList {
        /** A list of the OpenGL call lists for all of the centre faces. Array positions are accessed like this:
         * <p>
         * <code>
         * centerFaces[displayStage][facingOrdinal]</code>
         * <p>
         * Where displayStage is an integer between 0 and DISPLAY_STAGES - 1, and faceOrdinal is an integer between 0
         * and 5 */
        final int[][] centerFaces;

        /** A list of the OpenGL call lists for all of the side faces. Array positions are accessed like this:
         * <p>
         * <code>
         * sideFaces[displayStage][connectionFace][sideToRender]
         * </code>
         * <p>
         * Where displayStage is an integer between 0 and DISPLAY_STAGES -1, connectionFace is an integer between 0 and
         * 5 specifying the connection to render, and sideToRender is the side you currently want to render */
        final int[][][] sideFaces;

        DisplayFluidList(int[][] centerFaces, int[][][] sideFaces) {
            this.centerFaces = centerFaces;
            this.sideFaces = sideFaces;
        }
    }

    static void renderFluidPipe(Pipe<PipeTransportFluids> pipe, double x, double y, double z) {
        PipeTransportFluids trans = pipe.transport;

        boolean needsRender = false;
        FluidRenderData renderData = trans.renderCache;
        for (int i = 0; i < 7; ++i) {
            if (renderData.amount[i] > 0) {
                needsRender = true;
                break;
            }
        }

        if (!needsRender) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        // GlStateManager.enableCull();
        GlStateManager.disableLighting();
        // GlStateManager.enableBlend();
        // GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();

        GL11.glTranslatef((float) x, (float) y, (float) z);

        DisplayFluidList dfl = getDisplayFluidList(renderData.fluidID);
        RenderUtils.setGLColorFromInt(renderData.color);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        for (EnumFacing connection : EnumFacing.VALUES) {
            boolean connected = renderData.amount[connection.ordinal()] > 0;
            if (connected) {// Render the outer connection
                for (EnumFacing face : EnumFacing.VALUES) {// Render all the faces on this connection
                    if (face.getOpposite() == connection) {
                        continue;// If the face is facing the center of the pipe, don't render it
                    }
                    if (face == connection) {// && pipe.container.isPipeConnected(face)) {
                        continue;// If the face is connected to something else, don't render it
                    }

                    float amount = renderData.amount[connection.ordinal()] / (float) trans.getCapacity();
                    int stage = (int) (amount * (DISPLAY_STAGES - 1));
                    GL11.glPushMatrix();
                    GL11.glCallList(dfl.sideFaces[stage][connection.ordinal()][face.ordinal()]);
                    GL11.glPopMatrix();
                }
            } else {// Render the centre faces
                float amount = renderData.amount[6] / (float) trans.getCapacity();
                int stage = (int) (amount * (DISPLAY_STAGES - 1));
                GL11.glPushMatrix();
                GL11.glCallList(dfl.centerFaces[stage][connection.ordinal()]);
                GL11.glPopMatrix();
            }
        }

        // Do the thing here!

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        GL11.glPopAttrib();
        GL11.glPopMatrix();

    }

    private static DisplayFluidList getDisplayFluidList(int fluidID) {
        if (fluidLists.containsKey(fluidID)) {
            return fluidLists.get(fluidID);
        }

        Fluid fluid = FluidRegistry.getFluid(fluidID);
        TextureAtlasSprite sprite = FluidRenderer.getFluidTexture(fluid, false);

        int[][] center = new int[DISPLAY_STAGES][];

        for (int i = 0; i < DISPLAY_STAGES; i++) {
            center[i] = new int[6];
            for (EnumFacing face : EnumFacing.values()) {
                int ordinal = face.ordinal();
                center[i][ordinal] = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(center[i][ordinal], GL11.GL_COMPILE);

                GL11.glPushMatrix();
                GL11.glTranslated(0.5, 0.5, 0.5);
                GL11.glScalef(0.99f, 0.99f, 0.99f);
                GL11.glTranslated(-0.25, -0.25, -0.25);

                EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
                cuboid.iSize = 0.5;
                cuboid.jSize = 0.5 * ((i + 1) / (float) (DISPLAY_STAGES));
                cuboid.kSize = 0.5;
                cuboid.textures = new TextureAtlasSprite[6];
                cuboid.textures[ordinal] = sprite;

                RenderResizableCuboid.INSTANCE.renderCube(cuboid);

                GL11.glPopMatrix();
                GL11.glEndList();
            }
        }

        int[][][] connections = new int[DISPLAY_STAGES][][];

        for (int i = 0; i < DISPLAY_STAGES; i++) {
            connections[i] = new int[6][];
            for (EnumFacing connect : EnumFacing.values()) {
                int connectOrdinal = connect.ordinal();
                connections[i][connectOrdinal] = new int[6];

                EnumFacing pos = connect.getAxisDirection() == AxisDirection.POSITIVE ? connect : connect.getOpposite();
                
                Vec3 size = new Vec3(0.5, 0.5, 0.5).subtract(Utils.convert(pos, 0.25));
                Vec3 position = new Vec3(0.5, 0.5, 0.5).add(Utils.convert(connect, 0.25));
                position = position.subtract(Utils.multiply(new Vec3(0.5, 0.5, 0.5).add(Utils.convert(connect, 0.325)), 0.5));
                
                
                
                
                
                
                
                
                
                
                // The position is not correct!
                
                
                
                
                
                
                
                
                
                

                for (EnumFacing face : EnumFacing.values()) {
                    int ordinal = face.ordinal();
                    connections[i][connectOrdinal][ordinal] = GLAllocation.generateDisplayLists(1);
                    GL11.glNewList(connections[i][connectOrdinal][ordinal], GL11.GL_COMPILE);

                    GL11.glTranslated(position.xCoord, position.yCoord, position.zCoord);
                    GL11.glTranslated(size.xCoord / 2d, size.yCoord / 2d, size.zCoord / 2d);

                    GL11.glScalef(0.99f, 0.99f, 0.99f);

                    GL11.glTranslated(-size.xCoord / 2d, -size.yCoord / 2d, -size.zCoord / 2d);

                    EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
                    cuboid.iSize = size.xCoord;
                    cuboid.jSize = size.yCoord * ((i + 1) / (float) (DISPLAY_STAGES));
                    cuboid.kSize = size.zCoord;
                    cuboid.textures = new TextureAtlasSprite[6];
                    cuboid.textures[ordinal] = sprite;

                    RenderResizableCuboid.INSTANCE.renderCube(cuboid);

                    GL11.glEndList();
                }
            }
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        DisplayFluidList dfl = new DisplayFluidList(center, connections);
        fluidLists.put(fluidID, dfl);
        return dfl;
    }
}
