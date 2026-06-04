// STUB(R.Chen): Minecraft 1.12 ITickable — removed in 1.20. TODO: implement via BlockEntityTicker or TileBC_Neptune.tick().
package net.minecraft.util;

/**
 * Forge/Vanilla 1.12 ITickable — the block entity tick interface.
 * In Fabric 1.20.1, ticking block entities should register a BlockEntityTicker.
 * TODO(R.Chen): migrate all ITickable subclasses to BlockEntityTicker.
 */
public interface ITickable {
    void update();
}
