/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeDefinition;

// STUB(R.Chen): ItemPipeHolder — was extends ItemBC_Neptune (lib.item layer, not yet migrated).
// Implements IItemPipe so PipeRegistry.createItemForPipe can return a typed stub; full item
// (creative tab, model, colour sub-items) restored in Phase 4F when ItemBC_Neptune lands.
public class ItemPipeHolder extends Item implements IItemPipe {

    public static final List<ItemPipeHolder> items = new ArrayList<>();

    private final PipeDefinition definition;

    public ItemPipeHolder(PipeDefinition definition) {
        super(new Settings());
        this.definition = definition;
        items.add(this);
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }
}
