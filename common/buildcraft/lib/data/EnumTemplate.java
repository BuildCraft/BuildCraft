package buildcraft.lib.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;

public class EnumTemplate<E extends Enum<E>> implements IDataTemplate {
    private static final Map<Class<?>, EnumTemplate<?>> templates = new HashMap<>();

    public static <E extends Enum<E>> EnumTemplate<E> getForEnum(Class<E> clazz) {
        if (!templates.containsKey(clazz)) {
            templates.put(clazz, new EnumTemplate<>(clazz));
        }
        return (EnumTemplate<E>) templates.get(clazz);
    }

    private final String[] netToNbt;
    private final ImmutableMap<String, Integer> nbtToNet;

    private EnumTemplate(Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException(clazz + " was not an Enum!");
        netToNbt = new String[constants.length];
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        for (int i = 0; i < constants.length; i++) {
            E value = constants[i];
            String name = value.name().toLowerCase(Locale.ROOT);
            netToNbt[i] = name;
            builder.put(name, Integer.valueOf(i));
        }
        nbtToNet = builder.build();
    }

    @Override
    public void writeToPacketBuffer(NBTBase base, PacketBuffer buffer) {
        String name = base instanceof NBTTagString ? base.toString() : "";
        name = name.toLowerCase(Locale.ROOT);
        if (nbtToNet.containsKey(name)) {
            buffer.writeShort(nbtToNet.get(name).shortValue());
        } else {
            buffer.writeShort(0);
        }
    }

    @Override
    public NBTBase readFromPacketBuffer(PacketBuffer buffer) {
        int v = buffer.readShort();
        int length = netToNbt.length;
        v = (v % length + length) % length;
        return new NBTTagString(netToNbt[v]);
    }
}
