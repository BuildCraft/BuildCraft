/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.api.transport.pipe.IPipeHolder;

public abstract class ContainerPipe extends ContainerBC_Neptune {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(PlayerEntity player, int syncId, IPipeHolder pipeHolder) {
        super(player, syncId);
        this.pipeHolder = pipeHolder;
    }

    @Override
    public final boolean canUse(PlayerEntity player) {
        return pipeHolder.canPlayerInteract(player);
    }
}
