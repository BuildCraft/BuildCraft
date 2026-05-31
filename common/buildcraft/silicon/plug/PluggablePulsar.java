/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.plug;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

/**
 * Pipe pluggable for the pulsar (migrated stub).
 *
 * STUB(R.Chen): MjAPI/MjRedstoneReceiver, BCTransportConfig, PipeEventStatement,
 * expression model system, BCSiliconItems, SoundUtil all removed — deferred until
 * the respective lib / transport layers are migrated.
 */
public class PluggablePulsar extends PipePluggable {

    private static final int PULSE_STAGE = 20;

    private boolean manuallyEnabled = false;
    private int pulseStage = 0;
    private int gateEnabledTicks;
    private int gateSinglePulses;

    // Simple creator constructor
    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    // Saving + Loading
    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, NbtCompound nbt) {
        super(definition, holder, side);
        this.manuallyEnabled = nbt.getBoolean("manuallyEnabled");
        gateEnabledTicks = nbt.getInt("gateEnabledTicks");
        gateSinglePulses = nbt.getInt("gateSinglePulses");
        pulseStage = Math.max(0, Math.min(nbt.getInt("pulseStage"), PULSE_STAGE));
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();
        nbt.putBoolean("manuallyEnabled", manuallyEnabled);
        nbt.putInt("gateEnabledTicks", gateEnabledTicks);
        nbt.putInt("gateSinglePulses", gateSinglePulses);
        nbt.putInt("pulseStage", pulseStage);
        return nbt;
    }

    // Networking (net loader constructor)
    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, PacketByteBuf buffer) {
        super(definition, holder, side);
        // STUB(R.Chen): network data reading deferred — readData() removed until payload migrated.
    }

    @Override
    public Box getBoundingBox() {
        // STUB(R.Chen): Direction-specific bounding boxes deferred. Generic centred pulsar box.
        double min = 5 / 16.0;
        double max = 11 / 16.0;
        double ll  = 2 / 16.0;
        double uu  = 14 / 16.0;
        return new Box(min, ll, min, max, uu, max);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    public void enablePulsar() {
        gateEnabledTicks = 10;
    }

    public void addSinglePulse() {
        gateSinglePulses++;
    }

    private boolean isPulsing() {
        return manuallyEnabled || gateEnabledTicks > 0 || gateSinglePulses > 0;
    }
}
