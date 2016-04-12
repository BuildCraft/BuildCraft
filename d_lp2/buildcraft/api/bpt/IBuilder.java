package buildcraft.api.bpt;

import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IBuilder {
    World getWorld();

    BlockPos getPos();

    ImmutableSet<BptPermissions> getPermissions();

    /** @return The number of ticks the animation will take */
    int startBlockBuilding(BlockPos pos, ItemStack display, int delay);

    /** @return The number of ticks the animation will take */
    int startEntityBuilding(Vec3d target, ItemStack display, int delay);

    void addAction(IBptAction action, int delay);
}
