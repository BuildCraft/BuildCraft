package buildcraft.api.transport.pipe_bc8;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumPipePart implements IStringSerializable {
    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.EAST),
    EAST(EnumFacing.WEST),
    CENTER(null);

    private static Map<EnumFacing, EnumPipePart> facingMap = Maps.newEnumMap(EnumFacing.class);

    public final EnumFacing face;

    static {
        for (EnumPipePart part : values())
            if (part.face != null) {
                facingMap.put(part.face, part);
            }
    }

    public static EnumPipePart fromFacing(EnumFacing face) {
        if (face == null) return EnumPipePart.CENTER;
        return facingMap.get(face);
    }

    private EnumPipePart(EnumFacing face) {
        this.face = face;
    }

    public int getIndex() {
        if (face == null) return 6;
        return face.getIndex();
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public EnumPipePart next() {
        switch (this) {
            case DOWN:
                return EAST;
            case EAST:
                return NORTH;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return UP;
            case UP:
                return WEST;
            case WEST:
                return DOWN;
            default:
                return DOWN;
        }
    }
}
