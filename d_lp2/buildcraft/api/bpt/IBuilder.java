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

    void startBlockBuilding(BlockPos pos, ItemStack display, int delay);

    void startEntityBuilding(Vec3d target, ItemStack display, int delay);

    void addAction(IBptAction action);
}
