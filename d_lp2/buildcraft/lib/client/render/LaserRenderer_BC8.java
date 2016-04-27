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

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.client.render.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.LaserData_BC8.LaserSide;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;

public class LaserRenderer_BC8 {
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();
    private static final LoadingCache<LaserData_BC8, CompiledLaser> COMPILED_LASERS;

    static {
        COMPILED_LASERS = CacheBuilder.newBuilder()//
                .expireAfterAccess(10, TimeUnit.SECONDS)//
                .removalListener(LaserRenderer_BC8::removeCompiledLaser)//
                .build(CacheLoader.from(CompiledLaser::new));
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
            holding.setScale((float) data.scale);
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

        public VertexBuffer transformPos(Vec3d vec) {
            return transformPos(vec.xCoord, vec.yCoord, vec.zCoord);
        }

        public VertexBuffer transformPos(double x, double y, double z) {
            point.x = (float) x;
            point.y = (float) y;
            point.z = (float) z;
            matrix.transform(point);
            return buffer.pos(point.x, point.y, point.z);
        }
    }

    public static class CompiledLaser {
        private final int glListId;

        public CompiledLaser(LaserData_BC8 data) {
            CompiledLaserType type = compileType(data.laserType);
            // Draw the actual laser

            Tessellator tess = Tessellator.getInstance();
            VertexBuffer buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
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
        private final int startWidth, middleWidth, endWidth;
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
            // TODO: middle :P
            // Some sort of fair divide between start and end
            // then rendering out the rows in turn
        }
    }

    public static class InterpLaserRow {
        public final LaserRow[] rows;
        public final int width;
        public final int height;

        public InterpLaserRow(LaserRow row) {
            this(new LaserRow[] { row });
        }

        public InterpLaserRow(LaserRow[] rows) {
            if (rows.length < 1) throw new IllegalArgumentException("Not enough rows!");
            this.rows = rows;
            this.width = rows[0].width;
            this.height = rows[0].height;
        }

        public void bakeStartCap(LaserContext context) {
            final LaserRow row = rows[0];
            TextureAtlasSprite sprite = row.sprite.getSprite();
            int h = height / 2;
            // anti-clockwise
            context.transformPos(0, h, h).tex(sprite.getInterpolatedU(row.uMax), sprite.getInterpolatedV(row.vMax)).endVertex();
            context.transformPos(0, h, -h).tex(sprite.getInterpolatedU(row.uMax), sprite.getInterpolatedV(row.vMin)).endVertex();
            context.transformPos(0, -h, -h).tex(sprite.getInterpolatedU(row.uMin), sprite.getInterpolatedV(row.vMin)).endVertex();
            context.transformPos(0, -h, h).tex(sprite.getInterpolatedU(row.uMin), sprite.getInterpolatedV(row.vMax)).endVertex();
        }

        public void bakeEndCap(LaserContext context) {
            final LaserRow row = rows[0];
            TextureAtlasSprite sprite = row.sprite.getSprite();
            int h = height / 2;
            // clockwise
            context.transformPos(context.length, -h, h).tex(sprite.getInterpolatedU(row.uMin), sprite.getInterpolatedV(row.vMax)).endVertex();
            context.transformPos(context.length, -h, -h).tex(sprite.getInterpolatedU(row.uMin), sprite.getInterpolatedV(row.vMin)).endVertex();
            context.transformPos(context.length, h, -h).tex(sprite.getInterpolatedU(row.uMax), sprite.getInterpolatedV(row.vMin)).endVertex();
            context.transformPos(context.length, h, h).tex(sprite.getInterpolatedU(row.uMax), sprite.getInterpolatedV(row.vMax)).endVertex();
        }

        public void bakeFor(LaserSide side, double startX, int count, LaserContext context) {
            double xMin = startX;
            double xMax = startX + width;
            for (int i = 0; i < count; i++) {
                LaserRow row = rows[i % rows.length];
                if (side == LaserSide.TOP) {
                    // TODO
                } else if (side == LaserSide.BOTTOM) {
                    // TODO
                } else if (side == LaserSide.LEFT) {
                    // TODO
                } else if (side == LaserSide.RIGHT) {
                    // TODO
                }
                xMin += width;
                xMax += width;
            }
        }
    }
}
