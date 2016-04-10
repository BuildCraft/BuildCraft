/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.client.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.Maps;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.core.lib.EntityResizableCuboid;

public final class FluidRenderer {
    public static final FluidRenderer INSTANCE = new FluidRenderer();

    public enum FluidType {
        FLOWING,
        STILL,
        FROZEN
    }

    public static final int DISPLAY_STAGES = 100;
    public static final Vec3d BLOCK_SIZE = new Vec3d(0.98, 0.98, 0.98);
    private final Map<Fluid, Map<Vec3d, int[]>> flowingRenderCache = Maps.newHashMap();
    private final Map<Fluid, Map<Vec3d, int[]>> stillRenderCache = Maps.newHashMap();
    private final Map<Fluid, Map<Vec3d, int[]>> frozenRenderCache = Maps.newHashMap();
    private final Map<FluidType, Map<Fluid, TextureAtlasSprite>> textureMap = Maps.newHashMap();

    private static TextureAtlasSprite missingIcon = null;

    /** Deactivate default constructor */
    private FluidRenderer() {}

    @SubscribeEvent
    public void modelBakeEvent(ModelBakeEvent event) {
        flowingRenderCache.clear();
        stillRenderCache.clear();
        frozenRenderCache.clear();
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post event) {
        flowingRenderCache.clear();
        stillRenderCache.clear();
        frozenRenderCache.clear();
        TextureMap map = event.map;
        missingIcon = map.getMissingSprite();

        textureMap.clear();

        for (FluidType type : FluidType.values()) {
            textureMap.put(type, new HashMap<Fluid, TextureAtlasSprite>());
        }

        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            // TextureAtlasSprite toUse = null;

            if (fluid.getFlowing() != null) {
                String flow = fluid.getFlowing().toString();
                TextureAtlasSprite sprite;
                if (map.getTextureExtry(flow) != null) {
                    sprite = map.getTextureExtry(flow);
                } else {
                    sprite = map.registerSprite(fluid.getFlowing());
                }
                // toUse = sprite;
                textureMap.get(FluidType.FLOWING).put(fluid, sprite);
            }

            if (fluid.getStill() != null) {
                String still = fluid.getStill().toString();
                TextureAtlasSprite sprite;
                if (map.getTextureExtry(still) != null) {
                    sprite = map.getTextureExtry(still);
                } else {
                    sprite = map.registerSprite(fluid.getStill());
                }
                // toUse = sprite;
                textureMap.get(FluidType.STILL).put(fluid, sprite);
            }
            // if (toUse != null) {
            // textureMap.get(FluidType.FROZEN).put(fluid, toUse);
            // }
        }
    }

    public static TextureAtlasSprite getFluidTexture(FluidStack stack, FluidType type) {
        if (stack == null) {
            return missingIcon;
        }
        return getFluidTexture(stack.getFluid(), type);
    }

    /** This will always return a texture object, but it will be the missing icon texture if the fluid is null or a
     * texture does not exist. */
    public static TextureAtlasSprite getFluidTexture(Fluid fluid, FluidType type) {
        if (fluid == null || type == null) {
            return missingIcon;
        }
        Map<Fluid, TextureAtlasSprite> map = INSTANCE.textureMap.get(type);
        return map.containsKey(fluid) ? map.get(fluid) : missingIcon;
    }

    public static void setColorForFluidStack(FluidStack fluidstack) {
        if (fluidstack == null) {
            return;
        }

        int color = fluidstack.getFluid().getColor(fluidstack);
        RenderUtils.setGLColorFromInt(color);
    }

    /** Note that this does NOT implement caching. */
    public static int[] getFluidDisplayListForSide(FluidStack fluidStack, FluidType type, Vec3d size, EnumFacing side) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return null;
        }

        int[] lists = new int[DISPLAY_STAGES];

        for (int s = 0; s < DISPLAY_STAGES; ++s) {
            lists[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(lists[s], GL11.GL_COMPILE);

            EntityResizableCuboid ent = new EntityResizableCuboid(null);
            ent.xSize = size.xCoord;
            ent.ySize = (Math.max(s, 1) / (float) DISPLAY_STAGES) * size.yCoord;
            ent.zSize = size.zCoord;
            ent.texture = getFluidTexture(fluidStack, type);
            ent.makeClient();
            Arrays.fill(ent.textures, null);
            ent.textures[side.ordinal()] = ent.texture;

            RenderResizableCuboid.INSTANCE.renderCube(ent);

            GL11.glEndList();
        }

        return lists;
    }

    public static int[] getFluidDisplayLists(FluidStack fluidStack, FluidType type, Vec3d size) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return null;
        }
        Map<Fluid, Map<Vec3d, int[]>> cache = type == FluidType.FLOWING ? INSTANCE.flowingRenderCache : (type == FluidType.STILL
            ? INSTANCE.stillRenderCache : INSTANCE.frozenRenderCache);
        Map<Vec3d, int[]> displayLists = cache.get(fluid);
        int[] displayList;
        if (displayLists != null) {
            displayList = displayLists.get(size);
            if (displayList != null) {
                return displayList;
            }
        } else {
            displayLists = Maps.newHashMap();
            cache.put(fluid, displayLists);
        }

        displayList = new int[DISPLAY_STAGES];

        cache.put(fluid, displayLists);

        for (int s = 0; s < DISPLAY_STAGES; ++s) {
            displayList[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList[s], GL11.GL_COMPILE);

            EntityResizableCuboid ent = new EntityResizableCuboid(null);
            ent.xSize = size.xCoord;
            ent.ySize = (Math.max(s, 1) / (float) DISPLAY_STAGES) * size.yCoord;
            ent.zSize = size.zCoord;
            ent.texture = getFluidTexture(fluidStack, type);

            RenderResizableCuboid.INSTANCE.renderCube(ent, true, false);

            GL11.glEndList();
        }

        displayLists.put(size, displayList);

        return displayList;
    }
}
