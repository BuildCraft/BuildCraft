package buildcraft.builders.filling;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;

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
        new PacketBuffer(buf).writeString(parameter.getClass().getName());
        buf.writeInt(parameter.getOrdinal());
    }

    static IParameter fromBytes(ByteBuf buf) {
        try {
            // noinspection unchecked
            return ((Class<? extends IParameter>) Class.forName(new PacketBuffer(buf).readString(1024))).getEnumConstants()[buf.readInt()]; // FIXME: very dangerous
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
