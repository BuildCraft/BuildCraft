/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.render;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.EntityResizableCuboid;

public final class FluidRenderer {

    public static final int DISPLAY_STAGES = 100;
    private static Map<Fluid, int[]> flowingRenderCache = new HashMap<Fluid, int[]>();
    private static Map<Fluid, int[]> stillRenderCache = new HashMap<Fluid, int[]>();

    private static Map<Fluid, TextureAtlasSprite> flowingTextureMap = Maps.newHashMap();
    private static Map<Fluid, TextureAtlasSprite> stillTextureMap = Maps.newHashMap();
    private static TextureAtlasSprite missingIcon = null;

    /** Deactivate default constructor */
    private FluidRenderer() {

    }

    public static void initFluidTextures(TextureMap map) {
        // Because Fluid.getStillIcon is Deprecated and the fluid registry doesn't register icons properly (forge bug or
        // intentional?)
        missingIcon = map.getMissingSprite();

        flowingTextureMap.clear();
        stillTextureMap.clear();

        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            flowingTextureMap.put(fluid, map.registerSprite(fluid.getFlowing()));
            stillTextureMap.put(fluid, map.registerSprite(fluid.getStill()));
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

    public static int[] getFluidDisplayLists(FluidStack fluidStack, World world, boolean flowing) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return null;
        }
        Map<Fluid, int[]> cache = flowing ? flowingRenderCache : stillRenderCache;
        int[] diplayLists = cache.get(fluid);
        if (diplayLists != null) {
            return diplayLists;
        }

        diplayLists = new int[DISPLAY_STAGES];

        cache.put(fluid, diplayLists);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();

        for (int s = 0; s < DISPLAY_STAGES; ++s) {
            diplayLists[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(diplayLists[s], GL11.GL_COMPILE);

            EntityResizableCuboid ent = new EntityResizableCuboid(null);
            ent.iSize = 0.98;
            ent.jSize = (Math.max(s, 1) / (float) DISPLAY_STAGES) * 0.98;
            ent.kSize = 0.98;
            ent.texture = getFluidTexture(fluidStack, flowing);

            GL11.glTranslated(0.01, 0.01, 0.01);
            RenderResizableCuboid.INSTANCE.renderCube(ent);
            GL11.glTranslated(-0.01, -0.01, -0.01);

            GL11.glEndList();
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        return diplayLists;
    }
}
