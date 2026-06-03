// STUB(R.Chen): WorldClient removed in 1.20 — compile shim treating it as a World alias.
package net.minecraft.client.multiplayer;

import net.minecraft.world.World;

/** @deprecated Use {@link World} or ClientWorld instead. Compile shim only. */
public abstract class WorldClient extends World {
    protected WorldClient() { super(null, null, null, null, false); }
}
