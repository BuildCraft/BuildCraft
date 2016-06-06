package buildcraft.builders.tile;

import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.BptPermissions;
import buildcraft.lib.bpt.builder.AbstractBuilderAccessor;
import buildcraft.lib.misc.VecUtil;

public class TileBuilderAccessor extends AbstractBuilderAccessor {
    private final Vec3d vec;

    public TileBuilderAccessor(TileBuilder_Neptune tile, NBTTagCompound nbt) {
        super(tile.getOwner(), tile.getWorld(), tile.animation, nbt);
        this.vec = VecUtil.add(null, tile.getPos());
    }

    public TileBuilderAccessor(TileBuilder_Neptune tile) {
        super(tile.getOwner(), tile.getWorld(), tile.animation);
        this.vec = VecUtil.add(null, tile.getPos());
    }

    @Override
    public Vec3d getBuilderPosition() {
        return vec;
    }

    @Override
    public ImmutableSet<BptPermissions> getPermissions() {
        return ImmutableSet.of();
    }
}