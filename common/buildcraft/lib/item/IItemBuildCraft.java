/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.item;

public interface IItemBuildCraft {
    String id();

    default void init() {
        // STUB(R.Chen): Forge registry hooks (setUnlocalizedName/setRegistryName/setCreativeTab) dropped.
        // Fabric items are registered via Registry.register() in the module's ModInitializer.
    }

    /** Registers a metadata→model variant. STUB(R.Chen): model variant maps are deferred to the client
     * model-loading phase; this records the intent so callers compile. Phase 10. */
    default void addVariant(java.util.HashMap<Integer, net.minecraft.client.util.ModelIdentifier> variants, int meta, String suffix) {
        variants.put(meta, new net.minecraft.client.util.ModelIdentifier(
            new net.minecraft.util.Identifier("buildcraft", id() + "_" + suffix), "inventory"));
    }
}
