package ct.buildcraft.api.transport;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class WireNode {
    public final BlockPos pos;
    public final EnumWirePart part;
    private final int hash;

    public WireNode(BlockPos pos, EnumWirePart part) {
        this.pos = pos;
        this.part = part;
        hash = pos.hashCode() * 31 + part.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        WireNode other = (WireNode) obj;
        return part == other.part //
            && pos.equals(other.pos);
    }

    @Override
    public String toString() {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", " + part + ")";
    }

    public WireNode offset(Direction face) {
        int nx = (part.x == AxisDirection.POSITIVE ? 1 : 0) + face.getStepX();
        int ny = (part.y == AxisDirection.POSITIVE ? 1 : 0) + face.getStepY();
        int nz = (part.z == AxisDirection.POSITIVE ? 1 : 0) + face.getStepZ();
        EnumWirePart nPart = EnumWirePart.get(nx, ny, nz);
        if (nx < 0 || ny < 0 || nz < 0 || nx > 1 || ny > 1 || nz > 1) {
            return new WireNode(pos.offset(face.getNormal()), nPart);
        } else {
            return new WireNode(pos, nPart);
        }
    }

    public Map<Direction, WireNode> getAllPossibleConnections() {
        Map<Direction, WireNode> map = new EnumMap<>(Direction.class);

        for (Direction face : Direction.values()) {
            map.put(face, offset(face));
        }
        return map;
    }
}
