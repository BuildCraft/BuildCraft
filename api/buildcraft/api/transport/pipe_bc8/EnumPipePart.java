package buildcraft.api.transport.pipe_bc8;

import java.util.Locale;

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

    public final EnumFacing face;

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
}
