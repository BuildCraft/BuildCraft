/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.engines;

import java.util.Map;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.render.RenderResizableCuboid.EnumShadeArgument;
import buildcraft.core.lib.render.RenderResizableCuboid.IBlockLocation;
import buildcraft.core.lib.render.RenderResizableCuboid.IFacingLocation;
import buildcraft.core.lib.render.RenderResizableCuboid.RotatedFacingLocation;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.Utils;

public class RenderEngine extends TileEntitySpecialRenderer<TileEngineBase> {

    private static final float[] angleMap = new float[6];

    /** The number of stages to go through. Increase this number to go through more stages (smoother), decrease this
     * number to go through less stages (jumpier) */
    static {
        angleMap[EnumFacing.EAST.ordinal()] = (float) -Math.PI / 2;
        angleMap[EnumFacing.WEST.ordinal()] = (float) Math.PI / 2;
        angleMap[EnumFacing.UP.ordinal()] = 0;
        angleMap[EnumFacing.DOWN.ordinal()] = (float) Math.PI;
        angleMap[EnumFacing.SOUTH.ordinal()] = (float) Math.PI / 2;
        angleMap[EnumFacing.NORTH.ordinal()] = (float) -Math.PI / 2;
    }

    private TextureAtlasSprite spriteChamber;
    private final Map<EnumEngineType, TextureAtlasSprite> spriteBoxSide = Maps.newEnumMap(EnumEngineType.class);
    private final Map<EnumEngineType, TextureAtlasSprite> spriteBoxTop = Maps.newEnumMap(EnumEngineType.class);

    public RenderEngine() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post post) {
        spriteChamber = post.map.getAtlasSprite("buildcraftcore:blocks/engine/inv/chamber_base");
        for (EnumEngineType type : EnumEngineType.values()) {
            spriteBoxSide.put(type, post.map.getAtlasSprite(type.resourceLocation + "side"));
            spriteBoxTop.put(type, post.map.getAtlasSprite(type.resourceLocation + "back"));
        }
    }

    @Override
    public void renderTileEntityAt(TileEngineBase engine, double x, double y, double z, float f, int wtfIsThis) {

        if (engine != null) {
            World world = engine.getWorld();
            BlockPos pos = engine.getPos();
            IBlockState engineState = world.getBlockState(pos);
            if (engineState.getBlock() instanceof BlockEngineBase) {
                engineState = engineState.getBlock().getActualState(engineState, world, pos);

                if (BuildCraftProperties.MOVING.getValue(engineState)) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

                    // int light = world.getCombinedLight(pos, 0);
                    // int skyLight = light % (1 << 16);
                    // int blockLight = light / (1 << 16);
                    // OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, skyLight, blockLight);
                    // RenderHelper.enableStandardItemLighting();

                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y, z);

                    fireRenderer(engine.type, engine.orientation, engine.progress, pos);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    private void fireRenderer(EnumEngineType type, final EnumFacing face, float progress, BlockPos pos) {
        if (progress > 0.5) {
            progress = 1 - progress;
        }
        progress *= 2;
        // Display List
        {
            // listMap.get(type).renderStage(progress);
        }
        // Constant
        {
            final Vec3 coord = Utils.convert(pos);
            IBlockLocation locationFormula = new IBlockLocation() {
                @Override
                public Vec3 transformToWorld(Vec3 vec) {
                    return coord;
                }
            };

            IFacingLocation faceFormula = new RotatedFacingLocation(EnumFacing.UP, face);

            GL11.glPushMatrix();

            RenderUtils.translate(Utils.VEC_HALF);
            if (face == EnumFacing.DOWN) {
                GL11.glRotated(180, 1, 0, 0);
            } else if (face == EnumFacing.UP) {
                // Up is already correct
            } else {
                GL11.glRotated(90, 1, 0, 0);
                int angle = 0;
                EnumFacing tempFace = face;
                while (tempFace != EnumFacing.SOUTH) {
                    angle += 90;
                    tempFace = tempFace.rotateYCCW();
                }
                // rotate Z because we rotated the whole axis model to swap Y and Z (so we really rotate the Y axis)
                GL11.glRotated(angle, 0, 0, 1);
                // Rotate the X axis back (because we messed it up above). Which is now the Y axis because we just
                // swapped X and Z with Y I think... or something... IT WORKS OK SHUT UP.
                GL11.glRotated(-angle, 0, 1, 0);
            }
            RenderUtils.translate(Utils.vec3(-0.5));

            EntityResizableCuboid chamberCuboid = new EntityResizableCuboid(getWorld());
            chamberCuboid.texture = spriteChamber;
            chamberCuboid.setTextureOffset(new Vec3(3, 0, 3));

            Vec3 chamberSize = Utils.divide(new Vec3(10, progress * 8, 10), 16);
            chamberCuboid.setSize(chamberSize);

            Vec3 chamberOffset = Utils.divide(new Vec3(3, 4, 3), 16);

            RenderUtils.translate(chamberOffset);
            RenderResizableCuboid.INSTANCE.renderCube(chamberCuboid, EnumShadeArgument.FACE_LIGHT, locationFormula, faceFormula);
            RenderUtils.translate(Utils.multiply(chamberOffset, -1));

            EntityResizableCuboid boxCuboid = new EntityResizableCuboid(getWorld());
            boxCuboid.texture = spriteBoxSide.get(type);
            boxCuboid.makeClient();
            boxCuboid.textures[EnumFacing.UP.ordinal()] = spriteBoxTop.get(type);
            boxCuboid.textures[EnumFacing.DOWN.ordinal()] = spriteBoxTop.get(type);
            Vec3 boxSize = Utils.divide(new Vec3(16, 4, 16), 16);
            boxCuboid.setSize(boxSize);

            Vec3 boxOffset = new Vec3(0, 4 / 16d + progress / 2, 0);

            RenderUtils.translate(boxOffset);
            RenderResizableCuboid.INSTANCE.renderCube(boxCuboid, EnumShadeArgument.FACE_LIGHT, locationFormula, faceFormula);
            RenderUtils.translate(Utils.multiply(boxOffset, -1));

            GL11.glPopMatrix();
        }
    }
}
