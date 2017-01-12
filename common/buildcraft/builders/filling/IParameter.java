package buildcraft.builders.filling;

import buildcraft.lib.net.PacketBufferBC;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.Locale;

public interface IParameter {
    String getParameterName();

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
        nbt.setString("class", parameter.getClass().getName());
        nbt.setInteger("ordinal", parameter.getOrdinal());
        return nbt;
    }

    static IParameter readFromNBT(NBTTagCompound nbt) {
        try {
            // noinspection unchecked
            return ((Class<? extends IParameter>) Class.forName(nbt.getString("class"))).getEnumConstants()[nbt.getInteger("ordinal")];
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
