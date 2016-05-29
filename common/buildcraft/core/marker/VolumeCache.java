package buildcraft.core.marker;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.VolumeCache.SubCacheVolume;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.net.MessageMarker;

public class VolumeCache extends MarkerCache<SubCacheVolume> {
    public static final VolumeCache INSTANCE = new VolumeCache();

    private VolumeCache() {
        super("volume");
    }

    @Override
    protected SubCacheVolume createSubCache(World world) {
        return new SubCacheVolume(world);
    }

    public class SubCacheVolume extends MarkerCache.SubCache<VolumeConnection> {
        public SubCacheVolume(World world) {
            super(world, CACHES.indexOf(INSTANCE));
            // TODO: Load it from the world!
        }

        @Override
        public boolean tryConnect(BlockPos from, BlockPos to) {
            VolumeConnection fromConnection = getConnection(from);
            VolumeConnection toConnection = getConnection(to);
            if (fromConnection == null) {
                if (toConnection == null) {
                    return VolumeConnection.tryCreateConnection(this, from, to);
                } else {// The other one has a connection
                    return toConnection.addMarker(from);
                }
            } else {// We have a connection
                if (toConnection == null) {
                    return fromConnection.addMarker(to);
                } else {// The other one has a connection
                    return fromConnection.mergeWith(toConnection);
                }
            }
        }

        @Override
        public boolean canConnect(BlockPos from, BlockPos to) {
            VolumeConnection fromConnection = getConnection(from);
            VolumeConnection toConnection = getConnection(to);
            if (fromConnection == null) {
                if (toConnection == null) {
                    return VolumeConnection.canCreateConnection(this, from, to);
                } else {// The other one has a connection
                    return toConnection.canAddMarker(from);
                }
            } else {// We have a connection
                if (toConnection == null) {
                    return fromConnection.canAddMarker(to);
                } else {// The other one has a connection
                    return fromConnection.canMergeWith(toConnection);
                }
            }
        }

        @Override
        public ImmutableList<BlockPos> getValidConnections(BlockPos from) {
            VolumeConnection existing = getConnection(from);
            Set<Axis> taken = EnumSet.noneOf(EnumFacing.Axis.class);
            if (existing != null) {
                taken.addAll(existing.getConnectedAxis());
            }

            ImmutableList.Builder<BlockPos> valids = ImmutableList.builder();
            for (EnumFacing face : EnumFacing.values()) {
                if (taken.contains(face.getAxis())) continue;
                for (int i = 1; i < BCCoreConfig.markerMaxDistance; i++) {
                    BlockPos toTry = from.offset(face, i);
                    if (hasLoadedOrUnloadedMarker(toTry)) {
                        if (!canConnect(from, toTry)) break;
                        valids.add(toTry);
                        break;
                    }
                }
            }
            return valids.build();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public LaserType getPossibleLaserType() {
            return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
        }

        @Override
        @SideOnly(Side.CLIENT)
        protected boolean handleMessage(MessageMarker message) {
            List<BlockPos> positions = message.positions;
            if (message.connection) {
                if (message.add) {
                    for (BlockPos p : positions) {
                        VolumeConnection existing = this.getConnection(p);
                        destroyConnection(existing);
                    }
                    VolumeConnection con = new VolumeConnection(this, positions);
                    addConnection(con);
                } else { // removing from a connection
                    for (BlockPos p : positions) {
                        VolumeConnection existing = this.getConnection(p);
                        if (existing != null) {
                            existing.removeMarker(p);
                            refreshConnection(existing);
                        }
                    }
                }
            }
            return false;
        }
    }
}
