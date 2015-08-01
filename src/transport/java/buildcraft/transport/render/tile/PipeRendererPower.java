package buildcraft.transport.render.tile;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipeRendererPower {
    public static final float DISPLAY_MULTIPLIER = 0.1f;
    public static final int POWER_STAGES = 100;

    // static int[] displayPowerList = new int[POWER_STAGES];
    // static int[] displayPowerListOverload = new int[POWER_STAGES];

    private static int[][] normalPower = new int[POWER_STAGES][6];
    private static int[][] overloadPower = new int[POWER_STAGES][6];

    // private static final int[] angleY = { 0, 0, 270, 90, 0, 180 };
    // private static final int[] angleZ = { 90, 270, 0, 0, 0, 0 };

    private static boolean initialized = false;

    static void renderPowerPipe(Pipe<PipeTransportPower> pipe, double x, double y, double z) {
        initializeDisplayPowerList();

        PipeTransportPower pow = pipe.transport;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        // GL11.glEnable(GL11.GL_BLEND);

        GL11.glTranslatef((float) x, (float) y, (float) z);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        int[][] displayList = pow.overload > 0 ? overloadPower : normalPower;

        for (int side = 0; side < 6; ++side) {
            GL11.glPushMatrix();

            // GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            // GL11.glRotatef(angleY[side], 0, 1, 0);
            // GL11.glRotatef(angleZ[side], 0, 0, 1);
            // float scale = 1.0F - side * 0.0001F;
            // GL11.glScalef(scale, scale, scale);
            // GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

            short stage = pow.displayPower[side];
            if (stage >= 1) {
                if (stage < displayList.length) {
                    GL11.glCallList(displayList[stage][side]);
                } else {
                    GL11.glCallList(displayList[displayList.length - 1][side]);
                }
            }

            GL11.glPopMatrix();
        }

        // bindTexture(STRIPES_TEXTURE);
        // for (int side = 0; side < 6; side += 2) {
        // if (pipe.container.isPipeConnected(EnumFacing.values()[side])) {
        // GL11.glPushMatrix();
        // GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        // GL11.glRotatef(angleY[side], 0, 1, 0);
        // GL11.glRotatef(angleZ[side], 0, 0, 1);
        // float scale = 1.0F - side * 0.0001F;
        // GL11.glScalef(scale, scale, scale);
        // float movement = (0.50F) * pipe.transport.getPistonStage(side / 2);
        // GL11.glTranslatef(-0.25F - 1F / 16F - movement, -0.5F, -0.5F); //
        // float factor = (float) (1.0 / 256.0);
        // float factor = (float) (1.0 / 16.0);
        // box.render(factor);
        // GL11.glPopMatrix();
        // }
        // }

        GlStateManager.enableLighting();

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private static void initializeDisplayPowerList() {
        if (initialized) {
            return;
        }

        initialized = true;

        TextureAtlasSprite normal = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Power_Normal.ordinal());
        TextureAtlasSprite overloaded = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Power_Overload.ordinal());

        for (int stage = 0; stage < POWER_STAGES; stage++) {
            for (int side = 0; side < 6; side++) {
                int[] addresses = new int[2];

                addresses[0] = GLAllocation.generateDisplayLists(1);
                normalPower[stage][side] = addresses[0];

                addresses[1] = GLAllocation.generateDisplayLists(1);
                overloadPower[stage][side] = addresses[1];

                boolean overload = false;

                for (int address : addresses) {
                    GL11.glNewList(address, GL11.GL_COMPILE);

                    double width = 0.5 * stage / (double) POWER_STAGES;

                    EnumFacing face = EnumFacing.values()[side];

                    Vec3 pos = new Vec3(0.5, 0.5, 0.5).add(Utils.convert(face, 0.25));

                    face = Utils.convertPositive(face);
                    Vec3 size = new Vec3(1, 1, 1).subtract(Utils.convert(face));
                    size = Utils.multiply(size, width);
                    size = size.add(Utils.convert(face, 0.5));

                    EntityResizableCuboid erc = new EntityResizableCuboid(null);
                    erc.setSize(size);
                    erc.texture = overload ? overloaded : normal;

                    GL11.glPushMatrix();
                    RenderUtils.translate(pos);
                    RenderResizableCuboid.INSTANCE.renderCubeFromCentre(erc);
                    GL11.glPopMatrix();

                    GL11.glEndList();

                    overload = true;
                }
            }
        }
        //
        // RenderInfo block = new RenderInfo();
        // block.texture = normal;
        //
        // float size = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;
        //
        // for (int s = 0; s < POWER_STAGES; ++s) {
        // displayPowerList[s] = GLAllocation.generateDisplayLists(1);
        // GL11.glNewList(displayPowerList[s], GL11.GL_COMPILE);
        //
        // float minSize = 0.005F;
        //
        // float unit = (size - minSize) / 2F / POWER_STAGES;
        //
        // block.minY = 0.5 - (minSize / 2F) - unit * s;
        // block.maxY = 0.5 + (minSize / 2F) + unit * s;
        //
        // block.minZ = 0.5 - (minSize / 2F) - unit * s;
        // block.maxZ = 0.5 + (minSize / 2F) + unit * s;
        //
        // block.minX = 0;
        // block.maxX = 0.5 + (minSize / 2F) + unit * s;
        //
        // RenderEntityBlock.INSTANCE.renderBlock(block);
        //
        // GL11.glEndList();
        // }
        //
        // block.texture = overloaded;
        //
        // size = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;
        //
        // for (int s = 0; s < POWER_STAGES; ++s) {
        // displayPowerListOverload[s] = GLAllocation.generateDisplayLists(1);
        // GL11.glNewList(displayPowerListOverload[s], GL11.GL_COMPILE);
        //
        // float minSize = 0.005F;
        //
        // float unit = (size - minSize) / 2F / POWER_STAGES;
        //
        // block.minY = 0.5 - (minSize / 2F) - unit * s;
        // block.maxY = 0.5 + (minSize / 2F) + unit * s;
        //
        // block.minZ = 0.5 - (minSize / 2F) - unit * s;
        // block.maxZ = 0.5 + (minSize / 2F) + unit * s;
        //
        // block.minX = 0;
        // block.maxX = 0.5 + (minSize / 2F) + unit * s;
        //
        // RenderEntityBlock.INSTANCE.renderBlock(block);
        //
        // GL11.glEndList();
        // }
    }

    /** Called whenever a texture remap is done, to refresh the existing textures to new ones */
    // TODO (PASS 1): Call this from a post texture remap event!
    public static void resetTextures() {
        if (!initialized) {
            return;
        }
        initialized = false;

        for (int[] arr : normalPower) {
            for (int i : arr) {
                GLAllocation.deleteDisplayLists(i);
            }
        }

        for (int[] arr : overloadPower) {
            for (int i : arr) {
                GLAllocation.deleteDisplayLists(i);
            }
        }
    }
}
