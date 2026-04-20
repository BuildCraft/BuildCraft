package ct.buildcraft.api.enums;

public enum EnumSnapshotType {
    TEMPLATE(900),
    BLUEPRINT(300);

    public final int maxPerTick;

    EnumSnapshotType(int maxPerTick) {
        this.maxPerTick = maxPerTick;
    }
}
