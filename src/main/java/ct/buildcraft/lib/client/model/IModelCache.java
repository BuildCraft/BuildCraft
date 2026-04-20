/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface IModelCache<K> {
    List<BakedQuad> bake(K key);

    /** Clears all cached models. */
    void clear();
}
