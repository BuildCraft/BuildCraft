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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

public class LaserRenderer_BC8 {
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();
    private static final LoadingCache<LaserData_BC8, LaserCompiledList> COMPILED_GL_LASERS;
    private static final LoadingCache<LaserData_BC8, LaserCompiledBuffer> COMPILED_VB_LASERS;
    static final LoadingCache<BlockPos, Integer> CACHED_LIGHTMAP;

    public static final VertexFormat FORMAT_LESS, FORMAT_ALL;

    static {
        COMPILED_GL_LASERS = CacheBuilder.newBuilder()//
                .expireAfterWrite(5, TimeUnit.SECONDS)//
                .removalListener(LaserRenderer_BC8::removeCompiledLaser)//
                .build(CacheLoader.from(LaserRenderer_BC8::makeGlLaser));

        COMPILED_VB_LASERS = CacheBuilder.newBuilder()//
                .expireAfterWrite(5, TimeUnit.SECONDS)//
                .build(CacheLoader.from(LaserRenderer_BC8::makeVbLaser));

        // Really? Do we need to cache the lightmap?
        CACHED_LIGHTMAP = CacheBuilder.newBuilder()//
                .expireAfterWrite(1, TimeUnit.SECONDS)//
                .build(CacheLoader.from(LaserRenderer_BC8::computeLightmap));

        FORMAT_LESS = new VertexFormat();
        FORMAT_LESS.addElement(DefaultVertexFormats.POSITION_3F);
        FORMAT_LESS.addElement(DefaultVertexFormats.TEX_2F);
        FORMAT_LESS.addElement(DefaultVertexFormats.TEX_2S);

        FORMAT_ALL = new VertexFormat();
        FORMAT_ALL.addElement(DefaultVertexFormats.POSITION_3F);
        FORMAT_ALL.addElement(DefaultVertexFormats.TEX_2F);
        FORMAT_ALL.addElement(DefaultVertexFormats.TEX_2S);
        FORMAT_ALL.addElement(DefaultVertexFormats.COLOR_4UB);
        FORMAT_ALL.addElement(new VertexFormatElement(0, EnumType.FLOAT, EnumUsage.NORMAL, 3));
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

    private static LaserCompiledList makeGlLaser(LaserData_BC8 data) {
        LaserCompiledList.Builder renderer = new LaserCompiledList.Builder(data.enableDiffuse);
        makeLaser(data, renderer);
        return renderer.build();
    }

    private static LaserCompiledBuffer makeVbLaser(LaserData_BC8 data) {
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

    private static Integer computeLightmap(BlockPos pos) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return Integer.valueOf(0);
        int blockLight = getLightFor(world, EnumSkyBlock.BLOCK, pos);
        int skyLight = getLightFor(world, EnumSkyBlock.SKY, pos);
        return Integer.valueOf(skyLight << 20 | blockLight << 4);
    }

    private static int getLightFor(World world, EnumSkyBlock type, BlockPos pos) {
        int sum = 0;
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    int light = world.getLightFor(type, pos.add(x, y, z));
                    if (light > 0) {
                        sum += light;
                        count++;
                    }
                }
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    public static void renderLaserGlList(LaserData_BC8 data) {
        LaserCompiledList compiled = COMPILED_GL_LASERS.getUnchecked(data);
        compiled.render();
    }

    /** Assumes the buffer uses {@link DefaultVertexFormats#BLOCK} */
    public static void renderLaserBuffer(LaserData_BC8 data, VertexBuffer buffer) {
        LaserCompiledBuffer compiled = COMPILED_VB_LASERS.getUnchecked(data);
        compiled.render(buffer);
    }
}
