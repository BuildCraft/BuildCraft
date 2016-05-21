package buildcraft.lib.client.render;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import buildcraft.lib.client.render.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.LaserData_BC8.LaserSide;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;

public class LaserRenderer_BC8 {
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();
    private static final LoadingCache<LaserData_BC8, CompiledLaser> COMPILED_LASERS;
    private static final LoadingCache<BlockPos, Integer> CACHED_LIGHTMAP;
    private static final VertexFormat POSITION_TEX_LMAP;

    static {
        COMPILED_LASERS = CacheBuilder.newBuilder()//
                .expireAfterWrite(5, TimeUnit.SECONDS)//
                .removalListener(LaserRenderer_BC8::removeCompiledLaser)//
                .build(CacheLoader.from(CompiledLaser::new));

        CACHED_LIGHTMAP = CacheBuilder.newBuilder()//
                .expireAfterWrite(1, TimeUnit.SECONDS)//
                .build(CacheLoader.from(LaserRenderer_BC8::computeLightmap));

        POSITION_TEX_LMAP = new VertexFormat();
        POSITION_TEX_LMAP.addElement(DefaultVertexFormats.POSITION_3F);
        POSITION_TEX_LMAP.addElement(DefaultVertexFormats.TEX_2F);
        POSITION_TEX_LMAP.addElement(DefaultVertexFormats.TEX_2S);
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

    private static void removeCompiledLaser(RemovalNotification<LaserData_BC8, CompiledLaser> notification) {
        CompiledLaser comp = notification.getValue();
        if (comp != null) {
            comp.deleteGL();
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
        int val = world.getLightFor(type, pos);
        for (EnumFacing face : EnumFacing.values()) {
            val = Math.max(val, world.getLightFor(type, pos.offset(face)));
        }
        return val;
    }

    public static void renderLaser(LaserData_BC8 data) {
        CompiledLaser compiled = COMPILED_LASERS.getUnchecked(data);
        compiled.draw();
    }

    public static class LaserContext {
        public final Matrix4f matrix = new Matrix4f();
        private final Point3f point = new Point3f();
        private final VertexBuffer buffer;
        public final double length;

        public LaserContext(VertexBuffer buffer, LaserData_BC8 data) {
            this.buffer = buffer;
            Vec3d delta = data.start.subtract(data.end);
            double dx = delta.xCoord;
            double dy = delta.yCoord;
            double dz = delta.zCoord;

            double realLength = delta.lengthVector();
            length = realLength / data.scale;
            double angleZ = Math.PI - Math.atan2(dz, dx);
            dx = Math.sqrt(realLength * realLength - dy * dy);
            double angleY = -Math.atan2(dy, dx);

            // Matrix steps:
            // 1: rotate angles (Y) to make everything work
            // 2: rotate angles (Z) to make everything work
            // 3: scale it by the laser's scale
            // 4: translate forward by "start"

            matrix.setIdentity();
            Matrix4f holding = new Matrix4f();
            holding.setIdentity();

            // // Step 4
            Vector3f translation = new Vector3f();
            translation.x = (float) data.start.xCoord;
            translation.y = (float) data.start.yCoord;
            translation.z = (float) data.start.zCoord;
            holding.setTranslation(translation);
            matrix.mul(holding);
            holding.setIdentity();

            // Step 3
            holding.m00 = (float) data.scale;
            holding.m11 = (float) data.scale;
            holding.m22 = (float) data.scale;
            matrix.mul(holding);
            holding.setIdentity();

            // Step 2
            holding.rotY((float) angleZ);
            matrix.mul(holding);
            holding.setIdentity();

            // Step 1
            holding.rotZ((float) angleY);
            matrix.mul(holding);
            holding.setIdentity();
        }

        public void addPoint(double x, double y, double z, double u, double v) {
            point.x = (float) x;
            point.y = (float) y;
            point.z = (float) z;
            matrix.transform(point);
            buffer.pos(point.x, point.y, point.z);
            buffer.tex(u, v);
            computeLightMap(point);
            buffer.endVertex();
        }

        private void computeLightMap(Point3f point) {
            BlockPos pos = new BlockPos(point.x, point.y, point.z);
            Integer lmap = CACHED_LIGHTMAP.getUnchecked(pos);
            buffer.lightmap(lmap >> 16 & 65535, lmap & 65535);
        }
    }

    public static class CompiledLaser {
        private final int glListId;

        public CompiledLaser(LaserData_BC8 data) {
            CompiledLaserType type = compileType(data.laserType);
            // Draw the actual laser

            Tessellator tess = Tessellator.getInstance();
            VertexBuffer buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, POSITION_TEX_LMAP);
            LaserContext context = new LaserContext(buffer, data);

            type.bakeFor(context);

            glListId = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glListId, GL11.GL_COMPILE);
            tess.draw();
            GL11.glEndList();
        }

        public void deleteGL() {
            GLAllocation.deleteDisplayLists(glListId);
        }

        public void draw() {
            GL11.glCallList(glListId);
        }
    }

    public static class CompiledLaserType {
        public final LaserType type;
        private final InterpLaserRow startCap, endCap;
        private final InterpLaserRow start, end;
        private final double startWidth, middleWidth, endWidth;
        private final Map<LaserSide, InterpLaserRow> rows = new EnumMap<>(LaserSide.class);

        public CompiledLaserType(LaserType type) {
            this.type = type;
            this.startCap = new InterpLaserRow(type.capStart);
            this.endCap = new InterpLaserRow(type.capEnd);
            this.start = new InterpLaserRow(type.start);
            this.end = new InterpLaserRow(type.end);
            this.startWidth = start.width;
            this.endWidth = end.width;
            for (LaserSide side : LaserSide.VALUES) {
                List<LaserRow> validRows = new ArrayList<>();
                for (LaserRow row : type.variations) {
                    for (LaserSide inner : row.validSides) {
                        if (inner == side) {
                            validRows.add(row);
                            break;
                        }
                    }
                }
                rows.put(side, new InterpLaserRow(validRows.toArray(new LaserRow[validRows.size()])));
            }
            this.middleWidth = rows.get(LaserSide.BOTTOM).width;
        }

        public void bakeFor(LaserContext context) {
            startCap.bakeStartCap(context);
            endCap.bakeEndCap(context);

            double lengthForMiddle = Math.max(0, context.length - startWidth - endWidth);
            int numMiddle = MathHelper.floor_double(lengthForMiddle / middleWidth);
            double leftOverFromMiddle = lengthForMiddle - middleWidth * numMiddle;
            if (leftOverFromMiddle > 0) {
                numMiddle++;
            }
            double lengthEnds = context.length - middleWidth * numMiddle;
            double ratioStartEnd = startWidth / endWidth;
            double startLength = (lengthEnds / 2) * ratioStartEnd;
            double endLength = (lengthEnds / 2) / ratioStartEnd;
            start.bakeStart(context, startLength);
            end.bakeEnd(context, endLength);

            if (numMiddle > 0) {
                for (LaserSide side : LaserSide.VALUES) {
                    InterpLaserRow interp = rows.get(side);
                    interp.bakeFor(context, side, startLength, numMiddle);
                }
            }
        }
    }

    public static class InterpLaserRow {
        public final LaserRow[] rows;
        private final TextureAtlasSprite[] sprites;
        public final double width;
        public final double height;
        private int currentRowIndex;

        public InterpLaserRow(LaserRow row) {
            this(new LaserRow[] { row });
        }

        public InterpLaserRow(LaserRow[] rows) {
            if (rows.length < 1) throw new IllegalArgumentException("Not enough rows!");
            this.rows = rows;
            this.width = rows[0].width;
            this.height = rows[0].height;
            this.sprites = new TextureAtlasSprite[rows.length];
            for (int i = 0; i < rows.length; i++) {
                sprites[i] = rows[i].sprite.getSprite();
            }
        }

        private double texU(double between) {
            TextureAtlasSprite sprite = sprites[currentRowIndex];
            LaserRow row = rows[currentRowIndex];
            if (between == 0) return sprite.getInterpolatedU(row.uMin);
            if (between == 1) return sprite.getInterpolatedU(row.uMax);
            double interp = row.uMin * (1 - between) + row.uMax * between;
            return sprite.getInterpolatedU(interp);
        }

        private double texV(double between) {
            TextureAtlasSprite sprite = sprites[currentRowIndex];
            LaserRow row = rows[currentRowIndex];
            if (between == 0) return sprite.getInterpolatedV(row.vMin);
            if (between == 1) return sprite.getInterpolatedV(row.vMax);
            double interp = row.vMin * (1 - between) + row.vMax * between;
            return sprite.getInterpolatedV(interp);
        }

        public void bakeStartCap(LaserContext context) {
            this.currentRowIndex = 0;
            double h = height / 2;
            context.addPoint(0, h, h, texU(1), texV(1));
            context.addPoint(0, h, -h, texU(1), texV(0));
            context.addPoint(0, -h, -h, texU(0), texV(0));
            context.addPoint(0, -h, h, texU(0), texV(1));
        }

        public void bakeEndCap(LaserContext context) {
            this.currentRowIndex = 0;
            double h = height / 2;
            context.addPoint(context.length, -h, h, texU(0), texV(1));
            context.addPoint(context.length, -h, -h, texU(0), texV(0));
            context.addPoint(context.length, h, -h, texU(1), texV(0));
            context.addPoint(context.length, h, h, texU(1), texV(1));
        }

        public void bakeStart(LaserContext context, double length) {
            this.currentRowIndex = 0;
            final double h = height / 2;
            final double l = length;
            final double i = 1 - (length / width);
            // TOP
            context.addPoint(0, h, -h, texU(i), texV(0));// 1
            context.addPoint(0, h, h, texU(i), texV(1));// 2
            context.addPoint(l, h, h, texU(1), texV(1));// 3
            context.addPoint(l, h, -h, texU(1), texV(0));// 4
            // BOTTOM
            context.addPoint(l, -h, -h, texU(1), texV(0));// 4
            context.addPoint(l, -h, h, texU(1), texV(1));// 3
            context.addPoint(0, -h, h, texU(i), texV(1));// 2
            context.addPoint(0, -h, -h, texU(i), texV(0));// 1
            // LEFT
            context.addPoint(0, -h, -h, texU(i), texV(0));// 1
            context.addPoint(0, h, -h, texU(i), texV(1));// 2
            context.addPoint(l, h, -h, texU(1), texV(1));// 3
            context.addPoint(l, -h, -h, texU(1), texV(0));// 4
            // RIGHT
            context.addPoint(l, -h, h, texU(1), texV(0));// 4
            context.addPoint(l, h, h, texU(1), texV(1));// 3
            context.addPoint(0, h, h, texU(i), texV(1));// 2
            context.addPoint(0, -h, h, texU(i), texV(0));// 1
        }

        public void bakeEnd(LaserContext context, double length) {
            this.currentRowIndex = 0;
            final double h = height / 2;
            final double ls = context.length - length;
            final double lb = context.length;
            final double i = length / width;
            // TOP
            context.addPoint(ls, h, -h, texU(0), texV(0));// 1
            context.addPoint(ls, h, h, texU(0), texV(1));// 2
            context.addPoint(lb, h, h, texU(i), texV(1));// 3
            context.addPoint(lb, h, -h, texU(i), texV(0));// 4
            // BOTTOM
            context.addPoint(lb, -h, -h, texU(i), texV(0));// 4
            context.addPoint(lb, -h, h, texU(i), texV(1));// 3
            context.addPoint(ls, -h, h, texU(0), texV(1));// 2
            context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
            // LEFT
            context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
            context.addPoint(ls, h, -h, texU(0), texV(1));// 2
            context.addPoint(lb, h, -h, texU(i), texV(1));// 3
            context.addPoint(lb, -h, -h, texU(i), texV(0));// 4
            // RIGHT
            context.addPoint(lb, -h, h, texU(i), texV(0));// 4
            context.addPoint(lb, h, h, texU(i), texV(1));// 3
            context.addPoint(ls, h, h, texU(0), texV(1));// 2
            context.addPoint(ls, -h, h, texU(0), texV(0));// 1
        }

        public void bakeFor(LaserContext context, LaserSide side, double startX, int count) {
            double xMin = startX;
            double xMax = startX + width;
            double h = height / 2;
            for (int i = 0; i < count; i++) {
                this.currentRowIndex = i % rows.length;
                double ls = xMin;
                double lb = xMax;
                if (side == LaserSide.TOP) {
                    context.addPoint(ls, h, -h, texU(0), texV(0));// 1
                    context.addPoint(ls, h, h, texU(0), texV(1));// 2
                    context.addPoint(lb, h, h, texU(1), texV(1));// 3
                    context.addPoint(lb, h, -h, texU(1), texV(0));// 4
                } else if (side == LaserSide.BOTTOM) {
                    context.addPoint(lb, -h, -h, texU(1), texV(0));// 4
                    context.addPoint(lb, -h, h, texU(1), texV(1));// 3
                    context.addPoint(ls, -h, h, texU(0), texV(1));// 2
                    context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
                } else if (side == LaserSide.LEFT) {
                    context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
                    context.addPoint(ls, h, -h, texU(0), texV(1));// 2
                    context.addPoint(lb, h, -h, texU(1), texV(1));// 3
                    context.addPoint(lb, -h, -h, texU(1), texV(0));// 4
                } else if (side == LaserSide.RIGHT) {
                    context.addPoint(lb, -h, h, texU(1), texV(0));// 4
                    context.addPoint(lb, h, h, texU(1), texV(1));// 3
                    context.addPoint(ls, h, h, texU(0), texV(1));// 2
                    context.addPoint(ls, -h, h, texU(0), texV(0));// 1
                }
                xMin += width;
                xMax += width;
            }
        }
    }
}
