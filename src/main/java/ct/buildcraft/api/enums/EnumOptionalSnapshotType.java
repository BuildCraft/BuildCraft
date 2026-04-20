package ct.buildcraft.api.enums;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * Version of {@link EnumSnapshotType} with {@link EnumOptionalSnapshotType#NONE} value.
 * Shouldn't be used where it's possible to use {@link EnumSnapshotType}.
 */
public enum EnumOptionalSnapshotType implements StringRepresentable {
    NONE(null),
    TEMPLATE(EnumSnapshotType.TEMPLATE),
    BLUEPRINT(EnumSnapshotType.BLUEPRINT);

    public final EnumSnapshotType type;

    EnumOptionalSnapshotType(EnumSnapshotType type) {
        this.type = type;
    }

    public static EnumOptionalSnapshotType fromNullable(EnumSnapshotType type) {
        if (type == null) {
            return NONE;
        }
        switch (type) {
            case TEMPLATE:
                return TEMPLATE;
            case BLUEPRINT:
                return BLUEPRINT;
            default:
                return NONE;
        }
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
