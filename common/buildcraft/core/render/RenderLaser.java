/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.client.render.RenderResizableCuboid;
import buildcraft.core.lib.config.DetailedConfigOption;

public class RenderLaser extends Render<EntityLaser> {
    /** Option for the number of pixels that each laser should render as. */
    private static final DetailedConfigOption OPTION_LASER_FALLBACK_PIXELS = new DetailedConfigOption("render.laser.fallback.pixels", "2");

    // FIXME: REWRITE THE LASER RENDERER
    public static final float STEP = 0.04F;

    protected static ModelBase model = new ModelBase() {};
    private static ModelRenderer[] box;

    private static int[][] scaledBoxes;

    public RenderLaser() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    public static void onTextureReload() {
        scaledBoxes = null;
    }

    private static ModelRenderer getBox(int index) {
        if (box == null) {
            box = new ModelRenderer[40];

            for (int j = 0; j < box.length; ++j) {
                box[j] = new ModelRenderer(model, box.length - j, 0);
                box[j].addBox(0, -0.5F, -0.5F, 16, 1, 1);
                box[j].rotationPointX = 0;
                box[j].rotationPointY = 0;
                box[j].rotationPointZ = 0;
            }
        }

        return box[index];
    }

    private static void initScaledBoxes(World world) {
        if (scaledBoxes == null) {
            scaledBoxes = new int[100][20];

            for (int size = 0; size < 100; ++size) {
                for (int i = 0; i < 20; ++i) {
                    scaledBoxes[size][i] = GLAllocation.generateDisplayLists(1);
                    GL11.glNewList(scaledBoxes[size][i], GL11.GL_COMPILE);

                    EntityResizableCuboid cuboid = new EntityResizableCuboid(null);

                    float minSize = 0.2F * size / 100F;
                    float maxSize = 0.4F * size / 100F;
                    // float minSize = 0.1F;
                    // float maxSize = 0.2F;

                    float range = maxSize - minSize;

                    float diff = (float) (Math.cos(i / 20F * 2 * Math.PI) * range / 2F);

                    cuboid.setPosition(new Vec3(0, -maxSize / 2f - diff, -maxSize / 2f - diff));
                    cuboid.setSize(new Vec3(STEP, maxSize / 2f - diff, maxSize / 2f - diff));

                    RenderResizableCuboid.INSTANCE.renderCube(cuboid);

                    GL11.glEndList();
                }
            }
        }
    }

    @Override
    public void doRender(EntityLaser laser, double x, double y, double z, float f, float f1) {
        if (!laser.isVisible() || laser.getTexture() == null) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GlStateManager.disableLighting();

        Vec3 offset = laser.renderOffset();
        GL11.glTranslated(offset.xCoord, offset.yCoord, offset.zCoord);
        GL11.glTranslated(x - laser.data.head.xCoord, y - laser.data.head.yCoord, z - laser.data.head.zCoord);

        // FIXME: WARNING! not using getBox (laser) will kill laser movement.
        // we can use some other method for the animation though.
        doRenderLaser(laser.worldObj, renderManager.renderEngine, laser.data, laser.getTexture());

        GlStateManager.enableLighting();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void doRenderLaserWave(World world, TextureManager textureManager, LaserData laser, ResourceLocation texture) {
        if (!laser.isVisible || texture == null) {
            return;
        }

        GL11.glPushMatrix();

        GL11.glTranslated(laser.head.xCoord, laser.head.yCoord, laser.head.zCoord);
        laser.update();

        GL11.glRotatef((float) laser.angleZ, 0, 1, 0);
        GL11.glRotatef((float) laser.angleY, 0, 0, 1);

        textureManager.bindTexture(texture);

        int indexList = 0;

        initScaledBoxes(world);

        double x1 = laser.wavePosition;
        double x2 = x1 + scaledBoxes[0].length * STEP;
        double x3 = laser.renderSize;

        doRenderLaserLine(x1, laser.laserTexAnimation);

        for (double i = x1; i <= x2 && i <= laser.renderSize; i += STEP) {
            GL11.glCallList(scaledBoxes[(int) (laser.waveSize * 99F)][indexList]);
            indexList = (indexList + 1) % scaledBoxes[0].length;
            GL11.glTranslated(STEP, 0, 0);
        }

        if (x2 < x3) {
            doRenderLaserLine(x3 - x2, laser.laserTexAnimation);
        }

        GL11.glPopMatrix();
    }

    public static void doRenderLaser(World world, TextureManager textureManager, LaserData laser, ResourceLocation texture) {
        if (!laser.isVisible || texture == null) {
            return;
        }

        GL11.glPushMatrix();

        GL11.glTranslated(laser.head.xCoord, laser.head.yCoord, laser.head.zCoord);
        laser.update();

        GL11.glRotatef((float) laser.angleZ, 0, 1, 0);
        GL11.glRotatef((float) laser.angleY, 0, 0, 1);

        textureManager.bindTexture(texture);

        initScaledBoxes(world);

        doRenderLaserLine(laser.renderSize, laser.laserTexAnimation);

        GL11.glPopMatrix();

        // Render a constant width line to stop "aliasing" with lasers that are very far away
        // Deprecated GL but its only a single line per laser so it shouldn't be too bad
        GL11.glLineWidth(OPTION_LASER_FALLBACK_PIXELS.getAsInt());
        GL11.glBegin(GL11.GL_LINES);
        // The texture point at (1, 1) is always the light (not black) colour that we want.
        GL11.glTexCoord2d(0.9999d, 0.9999d);
        GL11.glVertex3d(laser.head.xCoord, laser.head.yCoord, laser.head.zCoord);
        GL11.glVertex3d(laser.tail.xCoord, laser.tail.yCoord, laser.tail.zCoord);
        GL11.glEnd();
    }

    private static void doRenderLaserLine(double len, int texId) {
        float lasti = 0;

        if (len - 1 > 0) {
            for (float i = 0; i <= len - 1; i += 1) {
                getBox(texId).render(1F / 16F);
                GL11.glTranslated(1, 0, 0);
                lasti = i;
            }
            lasti++;
        }

        GL11.glPushMatrix();
        GL11.glScalef((float) len - lasti, 1, 1);
        getBox(texId).render(1F / 16F);
        GL11.glPopMatrix();

        GL11.glTranslated((float) (len - lasti), 0, 0);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLaser entity) {
        return entity.getTexture();
    }
}
