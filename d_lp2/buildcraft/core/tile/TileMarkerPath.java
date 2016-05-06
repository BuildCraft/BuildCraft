package buildcraft.core.tile;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IPathProvider;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.MarkerCache;
import buildcraft.lib.tile.TileMarkerBase;

public class TileMarkerPath extends TileMarkerBase<TileMarkerPath> implements IPathProvider {
    public static final MarkerCache<TileMarkerPath> PATH_CACHE = createCache("bc:path");
    /** The path marker that comes before this. Used to dictate the direction of this path. Will be null if its not
     * connected to anything. */
    private BlockPos from, to;

    @Override
    protected TileMarkerPath getAsType() {
        return this;
    }

    @Override
    public MarkerCache<TileMarkerPath> getCache() {
        return PATH_CACHE;
    }

    @Override
    public boolean isActiveForRender() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public LaserType getPossibleLaserType() {
        return BuildCraftLaserManager.MARKER_PATH_POSSIBLE;
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getTo() {
        return to;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("");
        left.add("from = " + from);
        left.add("to = " + to);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected String getTypeInfo(BlockPos pos, TileMarkerPath marker) {
        if (pos.equals(from)) return TextFormatting.LIGHT_PURPLE + "F";
        else if (pos.equals(to)) return TextFormatting.GREEN + "T";
        return "";
    }

    @Override
    public boolean canConnectTo(TileMarkerPath other) {
        if (other == this) return false;
        if (connected.size() >= 2) return false;
        if (connected.containsKey(other.getPos())) return false;
        if (from == null && other.to == null) return true;
        if (to == null && other.from == null) return true;
        return false;
    }

    @Override
    protected void onConnect(TileMarkerPath other) {
        if (worldObj.isRemote) return;
        if (!connected.containsKey(other.getPos())) return;
        if (to == null && other.from == null && !Objects.equals(from, other.getPos())) {
            // Setup both variables so we don't screw anything up by doing them indervidually
            to = other.getPos();
            other.from = getPos();
        } else if (from == null && other.to == null && !Objects.equals(to, other.getPos())) {
            from = other.getPos();
            other.to = getPos();
        }
    }

    @Override
    protected void onDisconnect(TileMarkerPath other) {
        if (worldObj.isRemote) return;
        if (to != null && !connected.containsKey(to)) {
            to = null;
        }
        if (from != null && !connected.containsKey(from)) {
            from = null;
        }
    }

    public void reverseDirection() {
        Set<TileMarkerPath> tiles = gatherAllConnections();
        for (TileMarkerPath marker : tiles) {
            BlockPos from = marker.from;
            marker.from = marker.to;
            marker.to = from;
        }
        for (TileMarkerPath marker : tiles) {
            marker.sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    @Override
    public List<BlockPos> getPath() {
        LinkedList<BlockPos> positions = new LinkedList<>();

        positions.add(getPos());

        TileMarkerPath current = this;
        while ((current = getCacheForSide().get(current.from)) != null) {
            BlockPos pos = current.getPos();
            positions.addFirst(pos);
            if (positions.getLast().equals(pos)) break;
        }
        current = this;
        while ((current = getCacheForSide().get(current.to)) != null) {
            BlockPos pos = current.getPos();
            if (positions.contains(pos)) break;
            positions.addLast(pos);
        }

        BCLog.logger.info("Computed path:");
        for (BlockPos p : positions) {
            BCLog.logger.info(" - " + p);
        }
        return positions;
    }

    @Override
    public void removeFromWorld() {
        if (worldObj.isRemote) return;
        for (TileMarkerPath connectedTo : gatherAllConnections()) {
            worldObj.destroyBlock(connectedTo.getPos(), true);
        }
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);

        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                MessageUtil.writeBlockPosArray(buffer, new BlockPos[] { from, to });
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);

        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                BlockPos[] arr = MessageUtil.readBlockPosArray(buffer, 2);
                from = arr[0];
                to = arr[1];
            }
        }
    }
}
