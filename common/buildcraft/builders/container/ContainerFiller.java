/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): ContainerFiller deferred
package buildcraft.builders.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.net.IPayloadWriter;

import buildcraft.builders.tile.TileFiller;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {

    public ContainerFiller(TileFiller tile) {
        super(tile);
    }

    @Override
    public void sendMessage(int id, IPayloadWriter writer) {}

    @Override
    public boolean isInverted() { return false; }

    @Override
    public void setInverted(boolean value) {}

    @Override
    public void valuesChanged() {}
}
