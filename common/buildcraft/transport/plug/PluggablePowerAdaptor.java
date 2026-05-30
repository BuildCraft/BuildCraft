/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.plug;

// STUB(R.Chen): PluggablePowerAdaptor — heavy Forge MJ/RF caps:
//   - MjAPI.CAP_CONNECTOR/CAP_RECEIVER/CAP_REDSTONE_RECEIVER (Forge cap stubs)
//   - CapabilityEnergy/IEnergyStorage (Forge RF, not available)
//   - MjAPI.isRfAutoConversionEnabled / getRfConversion (Forge-only RF bridge)
// getCapability → stub null; restore in Phase 4E RF + Phase 4F cap layer.

import javax.annotation.Nullable;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;

public class PluggablePowerAdaptor extends PipePluggable {

    private static final Box[] BOXES = new Box[6];

    static {
        double ll = 0 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 16 / 16.0;

        double min = 3 / 16.0;
        double max = 13 / 16.0;

        BOXES[Direction.DOWN.ordinal()]  = new Box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()]    = new Box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = new Box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = new Box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()]  = new Box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()]  = new Box(ul, min, min, uu, max, max);
    }

    private long storedMJ = 0;

    public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side, NbtCompound nbt) {
        super(definition, holder, side);
        storedMJ = nbt.getLong("storedMJ");
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();
        nbt.putLong("storedMJ", storedMJ);
        return nbt;
    }

    @Override
    public Box getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        // STUB(R.Chen): BCTransportItems not in libLeaf (Forge RegistrationHelper dep). Phase 4F.
        return ItemStack.EMPTY;
    }

    @Override
    @Nullable
    public PluggableModelKey getModelRenderKey(RenderLayer layer) {
        if (layer == RenderLayer.getCutout()) {
            return new KeyPlugPowerAdaptor(side);
        }
        return null;
    }
}
