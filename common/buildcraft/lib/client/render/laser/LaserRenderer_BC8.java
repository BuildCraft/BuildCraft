/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import gnu.trove.map.hash.TLongIntHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.SpriteUtil;

@SideOnly(Side.CLIENT)
public class LaserRenderer_BC8 {
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();
    private static final LoadingCache<LaserData_BC8, LaserCompiledList> COMPILED_STATIC_LASERS;
    private static final LoadingCache<LaserData_BC8, LaserCompiledBuffer> COMPILED_DYNAMIC_LASERS;
    private static final TLongIntHashMap BLOCK_LIGHTMAP_CACHE = new TLongIntHashMap();
    private static final TLongIntHashMap SKY_LIGHTMAP_CACHE = new TLongIntHashMap();
    private static WeakReference<World> dynamicLaserWorld = new WeakReference<>(null);
    private static WeakReference<World> lightmapCacheWorld = new WeakReference<>(null);
    private static long lightmapCacheTick = Long.MIN_VALUE;

    public static final VertexFormat FORMAT_LESS, FORMAT_ALL;

    static {
        COMPILED_STATIC_LASERS = CacheBuilder.newBuilder()//
            .expireAfterWrite(5, TimeUnit.SECONDS)//
            .removalListener(LaserRenderer_BC8::removeCompiledLaser)//
            .build(CacheLoader.from(LaserRenderer_BC8::makeStaticLaser));

        COMPILED_DYNAMIC_LASERS = CacheBuilder.newBuilder()//
            .maximumSize(4096)//
            .expireAfterAccess(60, TimeUnit.SECONDS)//
            .build(CacheLoader.from(LaserRenderer_BC8::makeDynamicLaser));

        FORMAT_LESS = new VertexFormat();
        FORMAT_LESS.addElement(DefaultVertexFormats.POSITION_3F);
        FORMAT_LESS.addElement(DefaultVertexFormats.TEX_2F);
        FORMAT_LESS.addElement(DefaultVertexFormats.TEX_2S);

        FORMAT_ALL = new VertexFormat();
        FORMAT_ALL.addElement(DefaultVertexFormats.POSITION_3F);
        FORMAT_ALL.addElement(DefaultVertexFormats.TEX_2F);
        FORMAT_ALL.addElement(DefaultVertexFormats.TEX_2S);
        FORMAT_ALL.addElement(DefaultVertexFormats.COLOR_4UB);
    }

    public static void clearModels() {
        COMPILED_LASER_TYPES.clear();
        COMPILED_STATIC_LASERS.invalidateAll();
        COMPILED_DYNAMIC_LASERS.invalidateAll();
        dynamicLaserWorld = new WeakReference<>(null);
        clearLightmapCache();
    }

    private static CompiledLaserType compileType(LaserType laserType) {
        if (!COMPILED_LASER_TYPES.containsKey(laserType)) {
            COMPILED_LASER_TYPES.put(laserType, new CompiledLaserType(laserType));
        }
        return COMPILED_LASER_TYPES.get(laserType);
    }

    private static LaserCompiledList makeStaticLaser(LaserData_BC8 data) {
        try (LaserCompiledList.Builder renderer = new LaserCompiledList.Builder(data.enableDiffuse)) {
            makeLaser(data, renderer);
            return renderer.build();
        }
    }

    private static LaserCompiledBuffer makeDynamicLaser(LaserData_BC8 data) {
        LaserCompiledBuffer.Builder renderer = new LaserCompiledBuffer.Builder(data.enableDiffuse, data.minBlockLight);
        makeLaser(data, renderer);
        return renderer.build();
    }

    private static void makeLaser(LaserData_BC8 data, ILaserRenderer renderer) {
        LaserContext ctx = new LaserContext(renderer, data, data.enableDiffuse, data.doubleFace);
        CompiledLaserType type = compileType(data.laserType);
        type.bakeFor(ctx);
    }

    private static void removeCompiledLaser(RemovalNotification<LaserData_BC8, LaserCompiledList> notification) {
        LaserCompiledList comp = notification.getValue();
        if (comp != null) {
            comp.delete();
        }
    }

    public static int computeLightmap(double x, double y, double z, int minBlockLight) {
        World world = Minecraft.getMinecraft().world;
        if (world == null) return 0;
        updateLightmapCache(world);
        int blockLight =
            minBlockLight >= 15 ? 15 : Math.max(minBlockLight, getLightFor(world, EnumSkyBlock.BLOCK, x, y, z));
        int skyLight = getLightFor(world, EnumSkyBlock.SKY, x, y, z);
        return skyLight << 20 | blockLight << 4;
    }

    private static int getLightFor(World world, EnumSkyBlock type, double x, double y, double z) {
        TLongIntHashMap lightmapCache = type == EnumSkyBlock.BLOCK ? BLOCK_LIGHTMAP_CACHE : SKY_LIGHTMAP_CACHE;
        int max = 0;
        int count = 0;
        int sum = 0;

        boolean ao = Minecraft.isAmbientOcclusionEnabled();

        double xn = (x % 1 + 1) % 1;
        double yn = (y % 1 + 1) % 1;
        double zn = (z % 1 + 1) % 1;

        final double lowerBound = 0.3;
        final double upperBound = 1 - lowerBound;

        int xl = ao ? (xn < lowerBound ? -1 : 0) : -1;
        int yl = ao ? (yn < lowerBound ? -1 : 0) : -1;
        int zl = ao ? (zn < lowerBound ? -1 : 0) : -1;
        int xu = ao ? (xn > upperBound ? 1 : 0) : 1;
        int yu = ao ? (yn > upperBound ? 1 : 0) : 1;
        int zu = ao ? (zn > upperBound ? 1 : 0) : 1;

        for (int xp = xl; xp <= xu; xp++) {
            for (int yp = yl; yp <= yu; yp++) {
                for (int zp = zl; zp <= zu; zp++) {
                    long packedPos = packLightPos(x + xp, y + yp, z + zp);
                    int light;
                    if (lightmapCache.containsKey(packedPos)) {
                        light = lightmapCache.get(packedPos);
                    } else {
                        light = world.getLightFor(type, unpackLightPos(packedPos));
                        lightmapCache.put(packedPos, light);
                    }
                    if (light > 0) {
                        sum += light;
                        count++;
                    }
                    max = Math.max(max, light);
                }
            }
        }

        if (ao) {
            return count == 0 ? 0 : sum / count;
        } else {
            return max;
        }
    }

    private static void updateDynamicLaserWorld(World world) {
        if (dynamicLaserWorld.get() == world) {
            return;
        }
        COMPILED_DYNAMIC_LASERS.invalidateAll();
        dynamicLaserWorld = new WeakReference<>(world);
    }

    private static void updateLightmapCache(World world) {
        long worldTime = world.getTotalWorldTime();
        if (lightmapCacheWorld.get() == world && lightmapCacheTick == worldTime) {
            return;
        }
        BLOCK_LIGHTMAP_CACHE.clear();
        SKY_LIGHTMAP_CACHE.clear();
        lightmapCacheWorld = new WeakReference<>(world);
        lightmapCacheTick = worldTime;
    }

    private static void clearLightmapCache() {
        BLOCK_LIGHTMAP_CACHE.clear();
        SKY_LIGHTMAP_CACHE.clear();
        lightmapCacheWorld = new WeakReference<>(null);
        lightmapCacheTick = Long.MIN_VALUE;
    }

    private static long packLightPos(double x, double y, double z) {
        return packLightPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    private static long packLightPos(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) | ((long) z & 0x3FFFFFFL) << 12;
    }

    private static BlockPos unpackLightPos(long packedPos) {
        return new BlockPos((int) (packedPos >> 38), (int) (packedPos & 0xFFFL), (int) (packedPos << 26 >> 38));
    }

    public static void renderLaserStatic(LaserData_BC8 data) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("compute");
        LaserCompiledList compiled = COMPILED_STATIC_LASERS.getUnchecked(data);
        profiler.endStartSection("render");
        SpriteUtil.bindBlockTextureMap();
        compiled.render();
        profiler.endSection();
    }

    /** Assumes the buffer uses {@link DefaultVertexFormats#BLOCK} */
    public static void renderLaserDynamic(LaserData_BC8 data, BufferBuilder buffer) {
        Minecraft minecraft = Minecraft.getMinecraft();
        updateDynamicLaserWorld(minecraft.world);
        Profiler profiler = minecraft.mcProfiler;
        profiler.startSection("compute");
        LaserCompiledBuffer compiled = COMPILED_DYNAMIC_LASERS.getUnchecked(data);
        compiled.refreshLightmapIfNeeded(System.nanoTime());
        profiler.endStartSection("render");
        compiled.render(buffer);
        profiler.endSection();
    }
}
