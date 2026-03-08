/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model.key;

import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public final class PipeModelKey {
    public static final PipeModelKey DEFAULT_KEY;

    static {
        PipeFaceTex sprite = PipeFaceTex.get(0);
        PipeFaceTex[] sides = { sprite, sprite, sprite, sprite, sprite, sprite };
        float[] connected = { 0, 0, 0, 0, 0, 0 };
        DEFAULT_KEY = new PipeModelKey(null, sprite, sides, connected, null);
    }

    public final PipeDefinition definition;
    public final PipeFaceTex center;
    public final PipeFaceTex[] sides;
    public final float[] connected;
    public final DyeColor colour;
    private final int hash;

    public PipeModelKey(PipeDefinition definition, PipeFaceTex center, PipeFaceTex[] sides, float[] connected,
                        DyeColor colour) {
        this.definition = definition;
        this.center = center;
        this.sides = sides;
        this.connected = connected;
        this.colour = colour;
        this.hash = Arrays.hashCode(new int[] { //
                Objects.hashCode(definition), //
                Objects.hashCode(center), //
                Arrays.hashCode(sides), //
                Arrays.hashCode(connected), //
                Objects.hashCode(colour)//
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        PipeModelKey other = (PipeModelKey) obj;
        if (definition != other.definition) return false;
        if (center != other.center) return false;
        if (!Arrays.equals(sides, other.sides)) return false;
        if (!Arrays.equals(connected, other.connected)) return false;
        return colour == other.colour;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public EnumPipeColourType getColourType() {
        if (this.definition != null) {
            return definition.getColourType();
        } else {
            return EnumPipeColourType.TRANSLUCENT;
        }
    }
}
