/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.misc.SpriteUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LaserRenderer_BC8 {
    private static final LoadingCache<LaserData_BC8, LaserCompiledList> COMPILED_STATIC_LASERS;
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();
    private static final LoadingCache<LaserData_BC8, LaserCompiledBuffer> COMPILED_DYNAMIC_LASERS;

    public static final VertexFormat FORMAT_LESS, FORMAT_ALL;

    static {
        COMPILED_STATIC_LASERS = CacheBuilder.newBuilder()//
            .expireAfterWrite(5, TimeUnit.SECONDS)//
            .removalListener(LaserRenderer_BC8::removeCompiledLaser)//
            .build(CacheLoader.from(LaserRenderer_BC8::makeStaticLaser));

        COMPILED_DYNAMIC_LASERS = CacheBuilder.newBuilder()//
            .expireAfterWrite(5, TimeUnit.SECONDS)//
            .build(CacheLoader.from(LaserRenderer_BC8::makeDynamicLaser));

        FORMAT_LESS = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
        		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
        		.put("UV", DefaultVertexFormat.ELEMENT_UV)
        		.put("UV1", DefaultVertexFormat.ELEMENT_UV1)
        		.put("UV2", DefaultVertexFormat.ELEMENT_UV2)
        		.build());

        FORMAT_ALL = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
        		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
        		.put("UV", DefaultVertexFormat.ELEMENT_UV)
        		.put("UV1", DefaultVertexFormat.ELEMENT_UV1)
        		.put("Color", DefaultVertexFormat.ELEMENT_COLOR)
        		.put("UV2", DefaultVertexFormat.ELEMENT_UV2)
        		.build());

    }

    public static void clearModels() {
        COMPILED_LASER_TYPES.clear();
    }

    private static CompiledLaserType compileType(LaserType laserType) {
        if (!COMPILED_LASER_TYPES.containsKey(laserType)) {
            COMPILED_LASER_TYPES.put(laserType, new CompiledLaserType(laserType));
        }
        return COMPILED_LASER_TYPES.get(laserType);
    }

    private static LaserCompiledList makeStaticLaser(LaserData_BC8 data) {
        try (LaserCompiledList.Builder renderer = new LaserCompiledList.Builder(data.enableDiffuse)) {
            makeLaser(data, renderer, true);
            return renderer.build();
        }
    }

    private static LaserCompiledBuffer makeDynamicLaser(LaserData_BC8 data) {
        LaserCompiledBuffer.Builder renderer = new LaserCompiledBuffer.Builder(data.enableDiffuse);
        makeLaser(data, renderer, data.isOrgin);//default False
        return renderer.build();
    }

    private static void makeLaser(LaserData_BC8 data, ILaserRenderer renderer, boolean isStatic) {
        LaserContext ctx = new LaserContext(renderer, data, data.enableDiffuse, data.doubleFace, isStatic);
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
    	Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level == null) return 0;
        int blockLight =
            minBlockLight >= 15 ? 15 : Math.max(minBlockLight, getLightFor(level, LightLayer.BLOCK, x, y, z));
        int skyLight = getLightFor(level, LightLayer.SKY, x, y, z);
        return skyLight << 20 | blockLight << 4;
    }

    private static int getLightFor(Level level, LightLayer type, double x, double y, double z) {
        int max = 0;
        int count = 0;
        int sum = 0;
		boolean ao = Minecraft.useAmbientOcclusion();

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
                    int light = level.getBrightness(type, new BlockPos(x + xp, y + yp, z + zp));
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

    public static void renderLaserStatic(PoseStack pose, Matrix4f matrix, LaserData_BC8 data) {
        LaserCompiledList compiled = COMPILED_STATIC_LASERS.getUnchecked(data);//TODO
        SpriteUtil.bindBlockTextureMap();
        compiled.render(pose, matrix);
    }

    /** Assumes the buffer uses {@link DefaultVertexFormats#BLOCK} */
    public static void renderLaserDynamic(Matrix4f pose, Matrix3f normal, LaserData_BC8 data, VertexConsumer buffer) {
        LaserCompiledBuffer compiled = COMPILED_DYNAMIC_LASERS.getUnchecked(data);
        compiled.render(pose, normal, buffer);
    }
}
