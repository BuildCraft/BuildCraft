/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import java.util.Arrays;
import java.util.Locale;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.lib.net.PacketBufferBC;

public interface IParameter {
    default String getParameterName() {
        return EnumParameter.getForClass(getClass()).name().toLowerCase(Locale.ROOT);
    }

    default String getName() {
        return ((Enum<?>) this).name().toLowerCase(Locale.ROOT);
    }

    default int getOrdinal() {
        return ((Enum<?>) this).ordinal();
    }

    static void toBytes(ByteBuf buf, IParameter parameter) {
        new PacketBufferBC(buf).writeInt(EnumParameter.getForClass(parameter.getClass()).ordinal());
        new PacketBufferBC(buf).writeInt(parameter.getOrdinal());
    }

    static IParameter fromBytes(ByteBuf buf) {
        return EnumParameter.values()[buf.readInt()].clazz.getEnumConstants()[buf.readInt()];
    }

    static NBTTagCompound writeToNBT(NBTTagCompound nbt, IParameter parameter) {
        nbt.setInteger("type", EnumParameter.getForClass(parameter.getClass()).ordinal());
        nbt.setInteger("ordinal", parameter.getOrdinal());
        return nbt;
    }

    static IParameter readFromNBT(NBTTagCompound nbt) {
        return EnumParameter.values()[nbt.getInteger("type")].clazz.getEnumConstants()[nbt.getInteger("ordinal")];
    }

    enum EnumParameter {
        PATTERN(EnumParameterPattern.class),
        TYPE(EnumParameterType.class),
        AXIS(EnumParameterAxis.class);

        public final Class<? extends IParameter> clazz;

        EnumParameter(Class<? extends IParameter> clazz) {
            this.clazz = clazz;
        }

        public static EnumParameter getForClass(Class<? extends IParameter> clazz) {
            return Arrays.stream(values()).filter(enumTarget -> enumTarget.clazz == clazz).findFirst().orElse(null);
        }
    }
}
