/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.render;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.EntityResizableCuboid;

public final class FluidRenderer {

    public static final int DISPLAY_STAGES = 100;
    public static final Vec3 BLOCK_SIZE = new Vec3(0.98, 0.98, 0.98);

    private static Map<Fluid, Map<Vec3, int[]>> flowingRenderCache = Maps.newHashMap();
    private static Map<Fluid, Map<Vec3, int[]>> stillRenderCache = Maps.newHashMap();

    private static Map<Fluid, TextureAtlasSprite> stillTextureMap = Maps.newHashMap();
    private static Map<Fluid, TextureAtlasSprite> flowingTextureMap = Maps.newHashMap();
    private static TextureAtlasSprite missingIcon = null;

    /** Deactivate default constructor */
    private FluidRenderer() {

    }

    public static void initFluidTextures(TextureMap map) {
        missingIcon = map.getMissingSprite();

        stillTextureMap.clear();
        flowingTextureMap.clear();

        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            if (fluid.getStill() != null) {
                String still = fluid.getStill().toString();
                if (map.getTextureExtry(still) != null) {
                    stillTextureMap.put(fluid, map.getTextureExtry(still));
                } else {
                    stillTextureMap.put(fluid, map.registerSprite(fluid.getStill()));
                }
            }
            if (fluid.getFlowing() != null) {
                String flow = fluid.getFlowing().toString();
                if (map.getTextureExtry(flow) != null) {
                    flowingTextureMap.put(fluid, map.getTextureExtry(flow));
                } else {
                    flowingTextureMap.put(fluid, map.registerSprite(fluid.getStill()));
                }
            }
        }
    }

    public static TextureAtlasSprite getFluidTexture(FluidStack fluidStack, boolean flowing) {
        if (fluidStack == null) {
            return null;
        }
        return getFluidTexture(fluidStack.getFluid(), flowing);
    }

    public static TextureAtlasSprite getFluidTexture(Fluid fluid, boolean flowing) {
        if (fluid == null) {
            return null;
        }

        Map<Fluid, TextureAtlasSprite> map = flowing ? flowingTextureMap : stillTextureMap;

        if (!map.containsKey(fluid)) {
            return missingIcon;
        }

        TextureAtlasSprite icon = map.get(fluid);

        if (icon == null) {
            icon = missingIcon;
        }
        return icon;
    }

    public static void setColorForFluidStack(FluidStack fluidstack) {
        if (fluidstack == null) {
            return;
        }

        int color = fluidstack.getFluid().getColor(fluidstack);
        RenderUtils.setGLColorFromInt(color);
    }

    /** @deprecated Use {@link #getFluidDisplayLists(FluidStack,boolean,double,double,double)} instead */
    public static int[] getFluidDisplayLists(FluidStack fluidStack, boolean flowing) {
        return getFluidDisplayLists(fluidStack, flowing, BLOCK_SIZE);
    }

    /** Note that this does NOT implement caching. */
    public static int[] getFluidDisplayListForSide(FluidStack fluidStack, boolean flowing, Vec3 size, EnumFacing side) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return null;
        }

        int[] lists = new int[DISPLAY_STAGES];

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();

        for (int s = 0; s < DISPLAY_STAGES; ++s) {
            lists[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(lists[s], GL11.GL_COMPILE);

            EntityResizableCuboid ent = new EntityResizableCuboid(null);
            ent.xSize = size.xCoord;
            ent.ySize = (Math.max(s, 1) / (float) DISPLAY_STAGES) * size.yCoord;
            ent.zSize = size.zCoord;
            ent.texture = getFluidTexture(fluidStack, flowing);
            ent.makeClient();
            Arrays.fill(ent.textures, null);
            ent.textures[side.ordinal()] = ent.texture;

            RenderResizableCuboid.INSTANCE.renderCube(ent);

            GL11.glEndList();
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        return lists;
    }

    public static int[] getFluidDisplayLists(FluidStack fluidStack, boolean flowing, Vec3 size) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return null;
        }
        Map<Fluid, Map<Vec3, int[]>> cache = flowing ? flowingRenderCache : stillRenderCache;
        Map<Vec3, int[]> displayLists = cache.get(fluid);
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

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();

        for (int s = 0; s < DISPLAY_STAGES; ++s) {
            displayList[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList[s], GL11.GL_COMPILE);

            EntityResizableCuboid ent = new EntityResizableCuboid(null);
            ent.xSize = size.xCoord;
            ent.ySize = (Math.max(s, 1) / (float) DISPLAY_STAGES) * size.yCoord;
            ent.zSize = size.zCoord;
            ent.texture = getFluidTexture(fluidStack, flowing);

            RenderResizableCuboid.INSTANCE.renderCube(ent);

            GL11.glEndList();
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        displayLists.put(size, displayList);

        return displayList;
    }
}
