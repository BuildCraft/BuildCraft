// STUB(R.Chen): JsonToNBT / StringNbtReader shim.
package net.minecraft.nbt;

import java.io.IOException;

/** @deprecated Use {@link StringNbtReader} instead. Compile shim. */
public final class JsonToNBT {
    public static NbtCompound getTagFromJson(String json) {
        try { return StringNbtReader.parse(json); } catch (Exception e) { return new NbtCompound(); }
    }
}
