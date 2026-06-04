/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): legacy GL fluid renderer removed — porting to 1.20.1 VertexConsumer deferred
package buildcraft.lib.client.render.fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.compat.FluidStackBC;

@Environment(EnvType.CLIENT)
public class FluidRenderer {

    public static void renderFluid(FluidSpriteType type, FluidStackBC fluid,
            long amount, long capacity, TankSize size,
            MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        // STUB
    }

    public static void renderFluid(FluidSpriteType type, FluidStackBC fluid,
            long amount, long capacity, TankSize size,
            MatrixStack matrices, VertexConsumerProvider vcp, int light, boolean[] faces) {
        // STUB
    }

    public static class TankSize {
        public final Vec3d min, max;

        public TankSize(double sx, double sy, double sz, double ex, double ey, double ez) {
            this.min = new Vec3d(sx / 16.0, sy / 16.0, sz / 16.0);
            this.max = new Vec3d(ex / 16.0, ey / 16.0, ez / 16.0);
        }

        public TankSize(int sx, int sy, int sz, int ex, int ey, int ez) {
            this.min = new Vec3d(sx / 16.0, sy / 16.0, sz / 16.0);
            this.max = new Vec3d(ex / 16.0, ey / 16.0, ez / 16.0);
        }

        public TankSize(Vec3d min, Vec3d max) {
            this.min = min;
            this.max = max;
        }

        public TankSize shrink(double by) {
            return new TankSize(min.add(by, by, by), max.subtract(by, by, by));
        }

        public TankSize shrink(double x, double y, double z) {
            return new TankSize(min.add(x, y, z), max.subtract(x, y, z));
        }

        public TankSize shink(Vec3d by) {
            return new TankSize(min.add(by), max.subtract(by));
        }

        public TankSize rotateY() {
            Vec3d _min = new Vec3d(1 - max.z, min.y, min.x);
            Vec3d _max = new Vec3d(1 - min.z, max.y, max.x);
            return new TankSize(
                new Vec3d(Math.min(_min.x, _max.x), Math.min(_min.y, _max.y), Math.min(_min.z, _max.z)),
                new Vec3d(Math.max(_min.x, _max.x), Math.max(_min.y, _max.y), Math.max(_min.z, _max.z))
            );
        }

        public TankSize rotateYClockwise() { return rotateY(); }
    }
}
