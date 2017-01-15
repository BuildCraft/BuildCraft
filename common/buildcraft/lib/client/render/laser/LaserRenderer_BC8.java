package buildcraft.lib.client.render.laser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

public class LaserRenderer_BC8 {
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();
    private static final LoadingCache<LaserData_BC8, LaserCompiledList> COMPILED_STATIC_LASERS;
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
    }

    private static CompiledLaserType compileType(LaserType laserType) {
        if (!COMPILED_LASER_TYPES.containsKey(laserType)) {
            COMPILED_LASER_TYPES.put(laserType, new CompiledLaserType(laserType));
        }
        return COMPILED_LASER_TYPES.get(laserType);
    }

    private static LaserCompiledList makeStaticLaser(LaserData_BC8 data) {
        LaserCompiledList.Builder renderer = new LaserCompiledList.Builder(data.enableDiffuse);
        makeLaser(data, renderer);
        return renderer.build();
    }

    private static LaserCompiledBuffer makeDynamicLaser(LaserData_BC8 data) {
        LaserCompiledBuffer.Builder renderer = new LaserCompiledBuffer.Builder(data.enableDiffuse);
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
        int blockLight = minBlockLight >= 15 ? 15 : Math.max(minBlockLight, getLightFor(world, EnumSkyBlock.BLOCK, x, y, z));
        int skyLight = getLightFor(world, EnumSkyBlock.SKY, x, y, z);
        return skyLight << 20 | blockLight << 4;
    }

    private static int getLightFor(World world, EnumSkyBlock type, double x, double y, double z) {
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
                    int light = world.getLightFor(type, new BlockPos(x + xp, y + yp, z + zp));
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

    public static void renderLaserStatic(LaserData_BC8 data) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("compute");
        LaserCompiledList compiled = COMPILED_STATIC_LASERS.getUnchecked(data);
        profiler.endStartSection("render");
        compiled.render();
        profiler.endSection();
    }

    /** Assumes the buffer uses {@link DefaultVertexFormats#BLOCK} */
    public static void renderLaserDynamic(LaserData_BC8 data, VertexBuffer buffer) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("compute");
        LaserCompiledBuffer compiled = COMPILED_DYNAMIC_LASERS.getUnchecked(data);
        profiler.endStartSection("render");
        compiled.render(buffer);
        profiler.endSection();
    }
}
