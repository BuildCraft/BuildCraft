/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

// Could this be MutableBlockPos?
public class Translation {

    public double x = 0;
    public double y = 0;
    public double z = 0;

    public Vec3 translate(Vec3 p) {
        Vec3 p2 = new Vec3(p);

        p2.x = p.x + x;
        p2.y = p.y + y;
        p2.z = p.z + z;

        return p2;
    }

    public BlockPos translate(BlockPos pos) {
        return pos.add(x, y, z);
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
    }

}
